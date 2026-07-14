param(
    [string]$ComposeFile = "../docker-compose.yml",
    [string]$Container = "tpc-commerce-postgres",
    [string]$Database = "tpc_commerce",
    [string]$User = "tpc_admin",
    [string]$LogDir = "../../report/import_logs",
    [int]$ReadyTimeoutSeconds = 60,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$dbDir = Resolve-Path (Join-Path $scriptDir "..")
$composePath = Resolve-Path (Join-Path $scriptDir $ComposeFile)
$resolvedLogDir = Join-Path $scriptDir $LogDir
New-Item -ItemType Directory -Force -Path $resolvedLogDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = Join-Path $resolvedLogDir "migrate_db_$timestamp.log"

function Write-Log {
    param([string]$Message)
    $line = "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] $Message"
    Write-Host $line
    Add-Content -LiteralPath $logFile -Value $line -Encoding UTF8
}

function Invoke-LoggedCommand {
    param([string[]]$Command)

    Write-Log "Command: $($Command -join ' ')"
    if ($DryRun) {
        return ""
    }

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
        Add-Content -LiteralPath $logFile -Value $text -Encoding UTF8
    }
    if ($stderr) {
        $text = $stderr.TrimEnd()
        Write-Host $text
        Add-Content -LiteralPath $logFile -Value $text -Encoding UTF8
    }
    if ($process.ExitCode -ne 0) {
        throw "Command failed with exit code $($process.ExitCode): $($Command -join ' ')"
    }
    return $stdout.Trim()
}

Write-Log "Non-destructive database migration started. Database volume will be preserved."
Push-Location $dbDir
try {
    Invoke-LoggedCommand @("docker", "compose", "-f", $composePath, "up", "-d", "postgres") | Out-Null

    if (-not $DryRun) {
        $deadline = (Get-Date).AddSeconds($ReadyTimeoutSeconds)
        while ((Get-Date) -lt $deadline) {
            & docker exec $Container pg_isready -U $User -d $Database *> $null
            if ($LASTEXITCODE -eq 0) {
                break
            }
            Start-Sleep -Seconds 2
        }
        if ((Get-Date) -ge $deadline) {
            throw "PostgreSQL did not become ready within $ReadyTimeoutSeconds seconds."
        }
    }

    Invoke-LoggedCommand @(
        "docker", "exec", $Container, "psql", "-v", "ON_ERROR_STOP=1",
        "-U", $User, "-d", $Database, "-f", "/sql/V13__migrate_legacy_stock_change_log.sql"
    ) | Out-Null
    Invoke-LoggedCommand @(
        "docker", "exec", $Container, "psql", "-v", "ON_ERROR_STOP=1",
        "-U", $User, "-d", $Database, "-f", "/sql/V8__triggers.sql"
    ) | Out-Null

    $verifySql = @"
SELECT
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'stock_change_log' AND column_name = 'change_type'
  )::int || '|' ||
  EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'stock_change_log' AND column_name = 'change_reason'
  )::int || '|' ||
  EXISTS (
    SELECT 1 FROM pg_trigger
    WHERE tgname = 'trg_stock_quantity_change' AND NOT tgisinternal
  )::int;
"@
    $verification = Invoke-LoggedCommand @(
        "docker", "exec", $Container, "psql", "-v", "ON_ERROR_STOP=1",
        "-U", $User, "-d", $Database, "-qAt", "-c", $verifySql
    )

    if (-not $DryRun -and $verification.Trim() -ne "1|0|1") {
        throw "Migration verification failed. Expected change_type=1, change_reason=0, trigger=1; got '$verification'."
    }
    Write-Log "Migration verification: change_type present, change_reason absent, stock trigger present."
    Write-Log "Non-destructive migration completed. Existing PostgreSQL volume was not deleted."
}
finally {
    Pop-Location
}

Write-Host "Migration log saved to $logFile"
