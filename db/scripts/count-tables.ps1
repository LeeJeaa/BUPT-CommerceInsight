param(
    [string]$Container = "tpc-commerce-postgres",
    [string]$Database = "tpc_commerce",
    [string]$User = "tpc_admin",
    [string]$LogDir = "../../report/import_logs",
    [ValidatePattern("^[A-Za-z0-9._-]*$")]
    [string]$DataScale = ""
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$sqlFile = Resolve-Path (Join-Path $scriptDir "count-tables.sql")
$resolvedLogDir = Join-Path $scriptDir $LogDir
New-Item -ItemType Directory -Force -Path $resolvedLogDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$scaleSuffix = if ($DataScale) { "_sf$DataScale" } else { "" }
$logFile = Join-Path $resolvedLogDir "row_counts$($scaleSuffix)_$timestamp.txt"
$containerSql = "/tmp/count-tables.sql"

if ($DataScale) {
    Add-Content -Path $logFile -Value "Data scale: SF=$DataScale" -Encoding UTF8
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
    if ($stdout) {
        $text = $stdout.TrimEnd()
        Write-Host $text
        Add-Content -Path $logFile -Value $text -Encoding UTF8
    }
    if ($stderr) {
        $text = $stderr.TrimEnd()
        Write-Host $text
        Add-Content -Path $logFile -Value $text -Encoding UTF8
    }
    if ($process.ExitCode -ne 0) {
        throw "Command failed with exit code $($process.ExitCode): $($Command -join ' ')"
    }
}

Invoke-LoggedCommand @("docker", "cp", $sqlFile, "$Container`:$containerSql")
Invoke-LoggedCommand @("docker", "exec", $Container, "psql", "-v", "ON_ERROR_STOP=1", "-U", $User, "-d", $Database, "-f", $containerSql)

Write-Host "Row count log saved to $logFile"
