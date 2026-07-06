param(
    [ValidateSet("0.1", "0.2", "0.6", "1")]
    [string]$ScaleFactor = "0.2",
    [string]$DataDir = "",
    [ValidateSet("txt", "tbl")]
    [string]$FileExtension = "txt",
    [string]$Container = "tpc-commerce-postgres",
    [string]$Database = "tpc_commerce",
    [string]$User = "tpc_admin",
    [string]$LogDir = "../../report/import_logs",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
function Resolve-TpchDataDir {
    param([string]$InputDir)
    if ($InputDir) {
        return Resolve-Path -ErrorAction SilentlyContinue (Join-Path $scriptDir $InputDir)
    }

    $projectRoot = Resolve-Path (Join-Path $scriptDir "../..")
    $candidate = Get-ChildItem -LiteralPath $projectRoot -Directory |
        ForEach-Object { Get-ChildItem -LiteralPath $_.FullName -Directory -ErrorAction SilentlyContinue } |
        Where-Object { $_.Name -like "tpc-h*(2)" } |
        Select-Object -First 1

    if ($candidate) {
        return $candidate.FullName
    }

    return Resolve-Path -ErrorAction SilentlyContinue (Join-Path $scriptDir "../../data/tpch/SF0.1")
}

$resolvedDataDir = Resolve-TpchDataDir $DataDir
$resolvedLogDir = Join-Path $scriptDir $LogDir
New-Item -ItemType Directory -Force -Path $resolvedLogDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = Join-Path $resolvedLogDir "load_tpch_sf$($ScaleFactor)_$timestamp.log"

$tables = @(
    @{ Name = "region";   BaseFile = "region";   Columns = "r_regionkey,r_name,r_comment" },
    @{ Name = "nation";   BaseFile = "nation";   Columns = "n_nationkey,n_name,n_regionkey,n_comment" },
    @{ Name = "supplier"; BaseFile = "supplier"; Columns = "s_suppkey,s_name,s_address,s_nationkey,s_phone,s_acctbal,s_comment" },
    @{ Name = "customer"; BaseFile = "customer"; Columns = "c_custkey,c_name,c_address,c_nationkey,c_phone,c_acctbal,c_mktsegment,c_comment" },
    @{ Name = "part";     BaseFile = "part";     Columns = "p_partkey,p_name,p_mfgr,p_brand,p_type,p_size,p_container,p_retailprice,p_comment" },
    @{ Name = "partsupp"; BaseFile = "partsupp"; Columns = "ps_partkey,ps_suppkey,ps_availqty,ps_supplycost,ps_comment" },
    @{ Name = "orders";   BaseFile = "orders";   Columns = "o_orderkey,o_custkey,o_orderstatus,o_totalprice,o_orderdate,o_orderpriority,o_clerk,o_shippriority,o_comment" },
    @{ Name = "lineitem"; BaseFile = "lineitem"; Columns = "l_orderkey,l_partkey,l_suppkey,l_linenumber,l_quantity,l_extendedprice,l_discount,l_tax,l_returnflag,l_linestatus,l_shipdate,l_commitdate,l_receiptdate,l_shipinstruct,l_shipmode,l_comment" }
)

function Write-Log {
    param([string]$Message)
    $line = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $Message"
    Write-Host $line
    Add-Content -Path $logFile -Value $line
}

function Invoke-LoggedCommand {
    param([string[]]$Command)
    $psi = [System.Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = $Command[0]
    $psi.Arguments = (($Command[1..($Command.Length - 1)] | ForEach-Object {
        if ($_ -match '[\s"]') { '"' + ($_ -replace '"', '\"') + '"' } else { $_ }
    }) -join " ")
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.UseShellExecute = $false
    $process = [System.Diagnostics.Process]::Start($psi)
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()
    if ($stdout) { $stdout.TrimEnd() | Tee-Object -FilePath $logFile -Append }
    if ($stderr) { $stderr.TrimEnd() | Tee-Object -FilePath $logFile -Append }
    if ($process.ExitCode -ne 0) {
        throw "Command failed with exit code $($process.ExitCode): $($Command -join ' ')"
    }
}

Write-Log "TPC-H load started. SF=$ScaleFactor DataDir=$DataDir ResolvedDataDir=$resolvedDataDir FileExtension=$FileExtension Container=$Container"

if (-not $resolvedDataDir) {
    Write-Log "Data directory does not exist yet: $DataDir"
    Write-Log "Generate data first, then rerun this script. Use -DryRun to preview COPY commands."
    if (-not $DryRun) {
        throw "Missing data directory: $DataDir"
    }
}

foreach ($table in $tables) {
    $fileName = "$($table.BaseFile).$FileExtension"
    $hostFile = if ($resolvedDataDir) { Join-Path $resolvedDataDir $fileName } else { Join-Path $DataDir $fileName }
    $containerFile = "/tmp/tpch/$fileName"
    $normalizedFile = "/tmp/tpch/$($table.Name).csv"
    $copySql = "\copy $($table.Name) ($($table.Columns)) FROM '$normalizedFile' WITH (FORMAT csv, DELIMITER '|', NULL '', HEADER false)"

    Write-Log "Table $($table.Name): $hostFile"
    Write-Log "COPY SQL: $copySql"

    if ($DryRun) {
        continue
    }

    if (-not (Test-Path $hostFile)) {
        throw "Missing TPC-H data file: $hostFile"
    }

    Invoke-LoggedCommand @("docker", "exec", $Container, "mkdir", "-p", "/tmp/tpch")
    Invoke-LoggedCommand @("docker", "cp", $hostFile, "$Container`:$containerFile")
    Invoke-LoggedCommand @("docker", "exec", $Container, "bash", "-lc", "sed 's/|$//' '$containerFile' > '$normalizedFile'")
    Invoke-LoggedCommand @("docker", "exec", $Container, "psql", "-v", "ON_ERROR_STOP=1", "-U", $User, "-d", $Database, "-c", $copySql)
}

Write-Log "TPC-H load completed. Run count-tables.sql to verify row counts."
