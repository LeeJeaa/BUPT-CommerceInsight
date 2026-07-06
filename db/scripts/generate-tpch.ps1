param(
    [ValidateSet("0.1", "0.2", "0.6", "1")]
    [string]$ScaleFactor = "0.2",
    [string]$DbgenDir = "../../tools/tpch-dbgen",
    [string]$OutputRoot = "../../data/tpch",
    [string]$LogDir = "../../report/import_logs"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$resolvedDbgenDir = Resolve-Path -ErrorAction SilentlyContinue (Join-Path $scriptDir $DbgenDir)
$resolvedOutputRoot = Join-Path $scriptDir $OutputRoot
$resolvedLogDir = Join-Path $scriptDir $LogDir
$outputDir = Join-Path $resolvedOutputRoot "SF$ScaleFactor"

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
New-Item -ItemType Directory -Force -Path $resolvedLogDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = Join-Path $resolvedLogDir "generate_tpch_sf$($ScaleFactor)_$timestamp.log"

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

Write-Log "TPC-H dbgen started. SF=$ScaleFactor"

if (-not $resolvedDbgenDir) {
    Write-Log "dbgen directory not found: $DbgenDir"
    Write-Log "Place dbgen under tools/tpch-dbgen or pass -DbgenDir."
    throw "Missing dbgen directory."
}

$dbgenExe = Join-Path $resolvedDbgenDir "dbgen.exe"
if (-not (Test-Path $dbgenExe)) {
    $dbgenExe = Join-Path $resolvedDbgenDir "dbgen"
}
if (-not (Test-Path $dbgenExe)) {
    throw "Could not find dbgen executable in $resolvedDbgenDir"
}

Push-Location $outputDir
try {
    Write-Log "Running: $dbgenExe -s $ScaleFactor -f"
    Invoke-LoggedCommand @($dbgenExe, "-s", $ScaleFactor, "-f")
    Write-Log "Generated files:"
    Get-ChildItem -Filter "*.tbl" | Select-Object Name,Length | Format-Table | Out-String | Tee-Object -FilePath $logFile -Append
}
finally {
    Pop-Location
}

Write-Log "TPC-H dbgen completed. Output: $outputDir"
