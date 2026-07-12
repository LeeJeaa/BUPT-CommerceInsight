param(
    [ValidateSet("without_index", "with_index")]
    [string]$IndexMode = "with_index",
    [ValidateSet("all", "q1", "q5", "q12", "q14")]
    [string]$Query = "all",
    [string]$ScaleFactor = "0.1",
    [string]$Container = "tpc-commerce-postgres",
    [string]$Database = "tpc_commerce",
    [string]$User = "tpc_admin",
    [string]$OutputDir = "../../report/explain_plans",
    [string]$Q1ShipDate = "1998-09-02",
    [string]$Q5Region = "ASIA",
    [string]$Q5StartDate = "1994-01-01",
    [string]$Q5EndDate = "1995-01-01",
    [string]$Q12ShipMode1 = "MAIL",
    [string]$Q12ShipMode2 = "SHIP",
    [string]$Q12StartDate = "1994-01-01",
    [string]$Q12EndDate = "1995-01-01",
    [string]$Q14Month = "1995-09-01",
    [switch]$SkipIndexPreparation
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$resolvedOutputDir = Join-Path $scriptDir $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$tpchIndexes = @(
    "idx_lineitem_returnflag_linestatus",
    "idx_orders_orderdate",
    "idx_orders_custkey",
    "idx_lineitem_orderkey",
    "idx_lineitem_shipdate",
    "idx_lineitem_shipmode_receiptdate",
    "idx_customer_nationkey",
    "idx_supplier_nationkey",
    "idx_partsupp_suppkey",
    "idx_partsupp_partkey"
)

function Invoke-DockerPsql {
    param([string[]]$Arguments)
    & docker exec $Container psql -v ON_ERROR_STOP=1 -U $User -d $Database @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "psql command failed with exit code $LASTEXITCODE"
    }
}

if (-not $SkipIndexPreparation) {
    if ($IndexMode -eq "without_index") {
        $dropSql = ($tpchIndexes | ForEach-Object { "DROP INDEX IF EXISTS $_;" }) -join [Environment]::NewLine
        Write-Host "Preparing no-index state by dropping the V10 TPC-H secondary indexes."
        Invoke-DockerPsql @("-c", $dropSql)
    }
    else {
        Write-Host "Preparing indexed state from canonical sql/V10__indexes_baseline.sql."
        Invoke-DockerPsql @("-f", "/sql/V10__indexes_baseline.sql")
    }
}

Invoke-DockerPsql @("-c", "ANALYZE;")

$queries = [ordered]@{
    q1 = @"
SELECT
    l_returnflag AS return_flag,
    l_linestatus AS line_status,
    SUM(l_quantity) AS sum_quantity,
    SUM(l_extendedprice) AS sum_base_price,
    SUM(l_extendedprice * (1 - l_discount)) AS sum_discounted_price,
    SUM(l_extendedprice * (1 - l_discount) * (1 + l_tax)) AS sum_charge,
    AVG(l_quantity) AS avg_quantity,
    AVG(l_extendedprice) AS avg_price,
    AVG(l_discount) AS avg_disc,
    COUNT(*) AS count_order
FROM lineitem
WHERE l_shipdate <= DATE '$Q1ShipDate'
GROUP BY l_returnflag, l_linestatus
ORDER BY l_returnflag, l_linestatus
"@
    q5 = @"
SELECT
    TRIM(n.n_name) AS nation_name,
    SUM(l.l_extendedprice * (1 - l.l_discount)) AS revenue
FROM customer c
JOIN orders o ON c.c_custkey = o.o_custkey
JOIN lineitem l ON l.l_orderkey = o.o_orderkey
JOIN supplier s ON s.s_suppkey = l.l_suppkey
JOIN nation n ON n.n_nationkey = c.c_nationkey AND n.n_nationkey = s.s_nationkey
JOIN region r ON r.r_regionkey = n.n_regionkey
WHERE r.r_name = '$Q5Region'
  AND o.o_orderdate >= DATE '$Q5StartDate'
  AND o.o_orderdate < DATE '$Q5EndDate'
GROUP BY n.n_name
ORDER BY revenue DESC
"@
    q12 = @"
SELECT
    TRIM(l.l_shipmode) AS ship_mode,
    SUM(CASE WHEN o.o_orderpriority IN ('1-URGENT', '2-HIGH') THEN 1 ELSE 0 END) AS high_line_count,
    SUM(CASE WHEN o.o_orderpriority NOT IN ('1-URGENT', '2-HIGH') THEN 1 ELSE 0 END) AS low_line_count
FROM orders o
JOIN lineitem l ON o.o_orderkey = l.l_orderkey
WHERE l.l_shipmode IN ('$Q12ShipMode1', '$Q12ShipMode2')
  AND l.l_commitdate < l.l_receiptdate
  AND l.l_shipdate < l.l_commitdate
  AND l.l_receiptdate >= DATE '$Q12StartDate'
  AND l.l_receiptdate < DATE '$Q12EndDate'
GROUP BY TRIM(l.l_shipmode)
ORDER BY TRIM(l.l_shipmode)
"@
    q14 = @"
SELECT
    100.00 * SUM(CASE WHEN p.p_type LIKE 'PROMO%' THEN l.l_extendedprice * (1 - l.l_discount) ELSE 0 END)
    / NULLIF(SUM(l.l_extendedprice * (1 - l.l_discount)), 0) AS promo_revenue_percent
FROM lineitem l
JOIN part p ON p.p_partkey = l.l_partkey
WHERE l.l_shipdate >= DATE '$Q14Month'
  AND l.l_shipdate < (DATE '$Q14Month' + INTERVAL '1 month')::date
"@
}

$selectedQueries = if ($Query -eq "all") { @("q1", "q5", "q12", "q14") } else { @($Query) }
$outputFiles = @()

foreach ($queryName in $selectedQueries) {
    $sqlFile = Join-Path $env:TEMP "tpch_${queryName}_${IndexMode}_$timestamp.sql"
    $containerSql = "/tmp/tpch_${queryName}_${IndexMode}_$timestamp.sql"
    $outputFile = Join-Path $resolvedOutputDir "tpch_${queryName}_explain_${IndexMode}_sf$($ScaleFactor)_$timestamp.txt"
    $querySql = $queries[$queryName]
    $sql = @"
\pset pager off
\timing on

SELECT '$queryName' AS query_name,
       '$ScaleFactor' AS scale_factor,
       '$IndexMode' AS index_mode,
       current_timestamp AS captured_at;

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
$querySql;
"@

    try {
        Set-Content -LiteralPath $sqlFile -Value $sql -Encoding UTF8
        & docker cp $sqlFile "$Container`:$containerSql" | Out-Null
        if ($LASTEXITCODE -ne 0) { throw "docker cp failed for $queryName" }

        $planOutput = & docker exec $Container psql -v ON_ERROR_STOP=1 -U $User -d $Database -f $containerSql 2>&1
        $planExitCode = $LASTEXITCODE
        $planOutput | Write-Host
        $planOutput | Set-Content -LiteralPath $outputFile -Encoding UTF8
        if ($planExitCode -ne 0) { throw "EXPLAIN failed for $queryName" }
        $outputFiles += (Resolve-Path $outputFile).Path
        Write-Host "Saved $queryName $IndexMode EXPLAIN to $outputFile"
    }
    finally {
        Remove-Item -LiteralPath $sqlFile -Force -ErrorAction SilentlyContinue
        & docker exec $Container rm -f $containerSql *> $null
    }
}

$manifest = [ordered]@{
    capturedAt = (Get-Date).ToString("o")
    scaleFactor = $ScaleFactor
    indexMode = $IndexMode
    query = $Query
    threadCount = 1
    files = $outputFiles
    parameters = [ordered]@{
        q1ShipDate = $Q1ShipDate
        q5Region = $Q5Region
        q5StartDate = $Q5StartDate
        q5EndDate = $Q5EndDate
        q12ShipMode1 = $Q12ShipMode1
        q12ShipMode2 = $Q12ShipMode2
        q12StartDate = $Q12StartDate
        q12EndDate = $Q12EndDate
        q14Month = $Q14Month
    }
}
$manifestFile = Join-Path $resolvedOutputDir "tpch_explain_${IndexMode}_sf$($ScaleFactor)_$timestamp.json"
$manifest | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $manifestFile -Encoding UTF8
Write-Host "EXPLAIN manifest saved to $manifestFile"
