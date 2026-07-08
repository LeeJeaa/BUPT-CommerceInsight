param(
    [ValidateSet("without_index", "with_index")]
    [string]$IndexMode = "with_index",
    [string]$ScaleFactor = "0.2",
    [string]$Container = "tpc-commerce-postgres",
    [string]$Database = "tpc_commerce",
    [string]$User = "tpc_admin",
    [string]$OutputDir = "../../report/explain_plans",
    [string]$Q1ShipDate = "2020-12-31",
    [string]$Q5Region = "AFRICA",
    [string]$Q5StartDate = "2020-01-01",
    [string]$Q5EndDate = "2021-01-01",
    [string]$Q12StartDate = "2020-01-01",
    [string]$Q12EndDate = "2021-01-01",
    [string]$Q14Month = "2020-09-01"
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$resolvedOutputDir = Join-Path $scriptDir $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$outputFile = Join-Path $resolvedOutputDir "tpch_explain_${IndexMode}_sf$($ScaleFactor)_$timestamp.txt"
$sqlFile = Join-Path $env:TEMP "tpch_explain_${IndexMode}_$timestamp.sql"
$containerSql = "/tmp/tpch_explain_${IndexMode}_$timestamp.sql"

$sql = @"
\timing on
\pset pager off

SELECT 'metadata' AS section,
       '$ScaleFactor' AS scale_factor,
       '$IndexMode' AS index_mode,
       '$Q1ShipDate' AS q1_ship_date,
       '$Q5Region' AS q5_region,
       '$Q5StartDate' AS q5_start_date,
       '$Q5EndDate' AS q5_end_date,
       '$Q12StartDate' AS q12_start_date,
       '$Q12EndDate' AS q12_end_date,
       '$Q14Month' AS q14_month;

ANALYZE;

SELECT 'Q1 result_rows', COUNT(*) FROM (
    SELECT l_returnflag, l_linestatus
    FROM lineitem
    WHERE l_shipdate <= DATE '$Q1ShipDate'
    GROUP BY l_returnflag, l_linestatus
) q;

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
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
ORDER BY l_returnflag, l_linestatus;

SELECT 'Q5 result_rows', COUNT(*) FROM (
    SELECT n.n_name
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
) q;

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
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
ORDER BY revenue DESC;

SELECT 'Q12 result_rows', COUNT(*) FROM (
    SELECT TRIM(l.l_shipmode)
    FROM orders o
    JOIN lineitem l ON o.o_orderkey = l.l_orderkey
    WHERE l.l_shipmode IN ('MAIL', 'SHIP')
      AND l.l_commitdate < l.l_receiptdate
      AND l.l_shipdate < l.l_commitdate
      AND l.l_receiptdate >= DATE '$Q12StartDate'
      AND l.l_receiptdate < DATE '$Q12EndDate'
    GROUP BY TRIM(l.l_shipmode)
) q;

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT
    TRIM(l.l_shipmode) AS ship_mode,
    SUM(CASE WHEN o.o_orderpriority IN ('1-URGENT', '2-HIGH') THEN 1 ELSE 0 END) AS high_line_count,
    SUM(CASE WHEN o.o_orderpriority NOT IN ('1-URGENT', '2-HIGH') THEN 1 ELSE 0 END) AS low_line_count
FROM orders o
JOIN lineitem l ON o.o_orderkey = l.l_orderkey
WHERE l.l_shipmode IN ('MAIL', 'SHIP')
  AND l.l_commitdate < l.l_receiptdate
  AND l.l_shipdate < l.l_commitdate
  AND l.l_receiptdate >= DATE '$Q12StartDate'
  AND l.l_receiptdate < DATE '$Q12EndDate'
GROUP BY TRIM(l.l_shipmode)
ORDER BY TRIM(l.l_shipmode);

SELECT 'Q14 result_rows', COUNT(*) FROM (
    SELECT 1
    FROM lineitem l
    JOIN part p ON p.p_partkey = l.l_partkey
    WHERE l.l_shipdate >= DATE '$Q14Month'
      AND l.l_shipdate < (DATE '$Q14Month' + INTERVAL '1 month')::date
) q;

EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT
    100.00 * SUM(CASE WHEN p.p_type LIKE 'PROMO%' THEN l.l_extendedprice * (1 - l.l_discount) ELSE 0 END)
    / NULLIF(SUM(l.l_extendedprice * (1 - l.l_discount)), 0) AS promo_revenue_percent
FROM lineitem l
JOIN part p ON p.p_partkey = l.l_partkey
WHERE l.l_shipdate >= DATE '$Q14Month'
  AND l.l_shipdate < (DATE '$Q14Month' + INTERVAL '1 month')::date;
"@

Set-Content -LiteralPath $sqlFile -Value $sql -Encoding UTF8
docker cp $sqlFile "$Container`:$containerSql" | Out-Null
docker exec $Container psql -v ON_ERROR_STOP=1 -U $User -d $Database -f $containerSql | Tee-Object -FilePath $outputFile

Remove-Item -LiteralPath $sqlFile -Force
Write-Host "EXPLAIN output saved to $outputFile"
