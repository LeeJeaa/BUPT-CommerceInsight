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
Add-Check $checks "SQL assets present" (($sqlFiles | Measure-Object).Count -gt 0) "Expected V*.sql files under sql/."

$v13Path = Join-Path $root "sql/V13__migrate_legacy_stock_change_log.sql"
$initText = Get-Content -Raw -LiteralPath $initPath
Add-Check $checks "V13 migration asset present" (Test-Path $v13Path) $v13Path
Add-Check $checks "New-volume init invokes V13" ($initText -match "V13__migrate_legacy_stock_change_log\.sql") $initPath
$loadTpchText = Get-Content -Raw (Join-Path $root "db/scripts/load-tpch.ps1")
Add-Check $checks "Formal COPY deferral supported" (
    $initText -match "DEFER_POST_COPY_ASSETS" -and $loadTpchText -match "FinalizeSchema"
) "V1/V2/V3 -> COPY -> V4/V8/V9/V13/V10"
Add-Check $checks "Existing-volume migration entrypoint present" (Test-Path (Join-Path $root "db/scripts/migrate-db.ps1")) "db/scripts/migrate-db.ps1"

$resetPath = Join-Path $root "db/scripts/reset-db.ps1"
$resetText = if (Test-Path $resetPath) { Get-Content -Raw -LiteralPath $resetPath } else { "" }
Add-Check $checks "Reset requires explicit Force" ($resetText -match '\[switch\]\$Force' -and $resetText -match 'if \(-not \$Force\)') $resetPath
Add-Check $checks "TPC-H HTTP test entrypoint present" (Test-Path (Join-Path $root "test/tpch_concurrent_test.py")) "test/tpch_concurrent_test.py"
Add-Check $checks "TPC-C HTTP test entrypoint present" (Test-Path (Join-Path $root "test/tpcc_concurrent_test.py")) "test/tpcc_concurrent_test.py"

$checks | Format-Table -AutoSize

if (($checks | Where-Object { -not $_.Passed -and $_.Check -ne "SQL assets present" -and $_.Check -ne "Docker daemon running" }).Count -gt 0) {
    exit 1
}
