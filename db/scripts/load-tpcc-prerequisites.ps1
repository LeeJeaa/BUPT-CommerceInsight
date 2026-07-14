param(
    [string]$Container = "tpc-commerce-postgres",
    [string]$Database = "tpc_commerce",
    [string]$User = "tpc_admin",
    [string]$SampleAsset = "../../sql/V11__sample_data.sql",
    [string]$LogDir = "../../report/import_logs",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$samplePath = Resolve-Path (Join-Path $scriptDir $SampleAsset)
$resolvedLogDir = Join-Path $scriptDir $LogDir
New-Item -ItemType Directory -Force -Path $resolvedLogDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = Join-Path $resolvedLogDir "load_tpcc_prerequisites_course-minimal_$timestamp.log"
$containerSql = "/tmp/tpcc_prerequisites_$timestamp.sql"

function Write-Log {
    param([string]$Message)
    $line = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $Message"
    Write-Host $line
    Add-Content -Path $logFile -Value $line -Encoding UTF8
}

function Invoke-DockerPsql {
    param([string[]]$Arguments)
    $output = & docker exec $Container psql -v ON_ERROR_STOP=1 -U $User -d $Database @Arguments 2>&1
    $exitCode = $LASTEXITCODE
    if ($output) {
        $output | Write-Host
        $output | Add-Content -Path $logFile -Encoding UTF8
    }
    if ($exitCode -ne 0) {
        throw "psql failed with exit code $exitCode"
    }
    return $output
}

Write-Log "TPC-C prerequisite load started. Asset=$samplePath DryRun=$DryRun"

$countSql = @"
SELECT (SELECT count(*) FROM warehouse),
       (SELECT count(*) FROM district),
       (SELECT count(*) FROM tpcc_customer),
       (SELECT count(*) FROM item),
       (SELECT count(*) FROM stock);
"@
$countOutput = Invoke-DockerPsql @("-At", "-F", ",", "-c", $countSql)
$countLine = ($countOutput | Where-Object { $_ -match '^\d+,\d+,\d+,\d+,\d+$' } | Select-Object -Last 1)
if (-not $countLine) {
    throw "Could not read TPC-C prerequisite row counts."
}

$counts = @($countLine -split "," | ForEach-Object { [long]$_ })
$allEmpty = ($counts | Where-Object { $_ -ne 0 }).Count -eq 0
$allPresent = ($counts | Where-Object { $_ -le 0 }).Count -eq 0

if ($allPresent) {
    Write-Log "TPC-C prerequisites already exist. Counts=$countLine. No changes made."
    exit 0
}
if (-not $allEmpty) {
    throw "TPC-C prerequisite tables are partially populated (warehouse,district,tpcc_customer,item,stock=$countLine). Refusing to mix datasets."
}

$sampleText = Get-Content -Raw -LiteralPath $samplePath
$startMarker = "INSERT INTO warehouse ("
$endMarker = "-- password_hash"
$startIndex = $sampleText.IndexOf($startMarker, [System.StringComparison]::Ordinal)
$endIndex = $sampleText.IndexOf($endMarker, [System.StringComparison]::Ordinal)
if ($startIndex -lt 0 -or $endIndex -le $startIndex) {
    throw "Could not locate the canonical TPC-C prerequisite block in V11."
}

$prerequisiteSql = $sampleText.Substring($startIndex, $endIndex - $startIndex).Trim()
$sql = "BEGIN;`n$prerequisiteSql`nCOMMIT;`n"

if ($DryRun) {
    Write-Log "Dry run passed. Canonical V11 prerequisite block located; no changes made."
    exit 0
}

$hostSql = Join-Path $env:TEMP "tpcc_prerequisites_$timestamp.sql"
try {
    Set-Content -LiteralPath $hostSql -Value $sql -Encoding UTF8
    & docker cp $hostSql "$Container`:$containerSql" | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "docker cp failed for the TPC-C prerequisite SQL."
    }
    Invoke-DockerPsql @("-f", $containerSql) | Out-Null
}
finally {
    Remove-Item -LiteralPath $hostSql -Force -ErrorAction SilentlyContinue
    & docker exec $Container rm -f $containerSql *> $null
}

$verifiedOutput = Invoke-DockerPsql @("-At", "-F", ",", "-c", $countSql)
$verifiedLine = ($verifiedOutput | Where-Object { $_ -match '^\d+,\d+,\d+,\d+,\d+$' } | Select-Object -Last 1)
if ($verifiedLine -ne "1,1,1,2,2") {
    throw "Unexpected TPC-C prerequisite counts after load: $verifiedLine"
}

Write-Log "TPC-C prerequisites loaded from canonical V11. Counts=$verifiedLine"
