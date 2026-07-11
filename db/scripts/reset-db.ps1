param(
    [string]$ComposeFile = "../docker-compose.yml",
    [string]$LogDir = "../../report/import_logs",
    [switch]$KeepData
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$dbDir = Resolve-Path (Join-Path $scriptDir "..")
$composePath = Resolve-Path (Join-Path $scriptDir $ComposeFile)
$resolvedLogDir = Join-Path $scriptDir $LogDir
New-Item -ItemType Directory -Force -Path $resolvedLogDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = Join-Path $resolvedLogDir "reset_db_$timestamp.log"

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

Write-Log "Reset started. Compose: $composePath"
Push-Location $dbDir
try {
    if ($KeepData) {
        Write-Log "Stopping and starting containers without deleting volumes."
        Invoke-LoggedCommand @("docker", "compose", "-f", $composePath, "down")
    }
    else {
        Write-Log "Stopping containers and deleting database volumes."
        Invoke-LoggedCommand @("docker", "compose", "-f", $composePath, "down", "-v")
    }

    Write-Log "Starting PostgreSQL."
    Invoke-LoggedCommand @("docker", "compose", "-f", $composePath, "up", "-d")

    Write-Log "Container status."
    Invoke-LoggedCommand @("docker", "compose", "-f", $composePath, "ps")
    Write-Log "Reset completed."
}
finally {
    Pop-Location
}
