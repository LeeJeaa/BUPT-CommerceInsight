param(
    [string]$ComposeFile = "../docker-compose.yml",
    [string]$InitScript = "../init/00_run_sql_assets.sh"
)

$ErrorActionPreference = "Continue"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$composePath = Resolve-Path (Join-Path $scriptDir $ComposeFile)
$initPath = Resolve-Path (Join-Path $scriptDir $InitScript)
$root = Resolve-Path (Join-Path $scriptDir "../..")

function Add-Check {
    param(
        [System.Collections.Generic.List[object]]$Checks,
        [string]$Name,
        [bool]$Passed,
        [string]$Detail
    )
    $Checks.Add([pscustomobject]@{
        Check = $Name
        Passed = $Passed
        Detail = $Detail
    }) | Out-Null
}

$checks = [System.Collections.Generic.List[object]]::new()

Add-Check $checks "Docker CLI installed" ([bool](Get-Command docker -ErrorAction SilentlyContinue)) "Required for PostgreSQL Docker."

$dockerInfo = docker info 2>&1
Add-Check $checks "Docker daemon running" ($LASTEXITCODE -eq 0) (($dockerInfo | Select-Object -First 1) -join "")

$composeConfig = docker compose -f $composePath config 2>&1
Add-Check $checks "Compose config valid" ($LASTEXITCODE -eq 0) "docker compose -f db/docker-compose.yml config"

$initBytes = [System.IO.File]::ReadAllBytes($initPath)
Add-Check $checks "Init script LF only" (-not ($initBytes -contains 13)) "db/init/00_run_sql_assets.sh"

$requiredDirs = @("sql", "db/init", "db/scripts", "report/import_logs", "report/explain_plans", "report/performance", "test")
foreach ($dir in $requiredDirs) {
    $path = Join-Path $root $dir
    Add-Check $checks "Directory exists: $dir" (Test-Path $path) $path
}

$sqlFiles = Get-ChildItem -Path (Join-Path $root "sql") -Filter "V*.sql" -ErrorAction SilentlyContinue
Add-Check $checks "SQL assets present" (($sqlFiles | Measure-Object).Count -gt 0) "Expected A's V*.sql files under sql/; missing is OK before A delivery."

$checks | Format-Table -AutoSize

if (($checks | Where-Object { -not $_.Passed -and $_.Check -ne "SQL assets present" -and $_.Check -ne "Docker daemon running" }).Count -gt 0) {
    exit 1
}
