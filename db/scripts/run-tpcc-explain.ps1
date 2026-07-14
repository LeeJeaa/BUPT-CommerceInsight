param(
    [ValidateSet("all", "new-order", "payment")]
    [string]$Transaction = "all",
    [ValidatePattern("^[A-Za-z0-9._-]+$")]
    [string]$DataScale = "course-minimal",
    [string]$Container = "tpc-commerce-postgres",
    [string]$Database = "tpc_commerce",
    [string]$User = "tpc_admin",
    [string]$OutputDir = "../../report/explain_plans",
    [ValidateRange(1, 2147483647)]
    [int]$WarehouseId = 1,
    [ValidateRange(1, 2147483647)]
    [int]$DistrictId = 1,
    [ValidateRange(1, 2147483647)]
    [int]$CustomerId = 1,
    [ValidateRange(1, 2147483647)]
    [int]$ItemId = 1001,
    [ValidateRange(1, 100)]
    [int]$Quantity = 1,
    [ValidateRange(0.01, 999999999.99)]
    [decimal]$PaymentAmount = 25.00
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$resolvedOutputDir = Join-Path $scriptDir $OutputDir
New-Item -ItemType Directory -Force -Path $resolvedOutputDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$amount = $PaymentAmount.ToString("0.00", [System.Globalization.CultureInfo]::InvariantCulture)

$transactions = if ($Transaction -eq "all") {
    @("new-order", "payment")
}
else {
    @($Transaction)
}

$sqlByTransaction = [ordered]@{
    "new-order" = @"
\pset pager off
\timing on

SELECT 'new-order' AS transaction_name,
       '$DataScale' AS data_scale,
       current_timestamp AS captured_at;

BEGIN;
SELECT set_config('app.transaction_id', 'explain-new-order-$timestamp', true);
SELECT set_config('app.change_type', 'new_order', true);

SELECT d_next_o_id AS order_id
FROM district
WHERE d_w_id = $WarehouseId AND d_id = $DistrictId
\gset

\echo 'statement=contextLookup'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT w.w_id
FROM warehouse w
JOIN district d ON d.d_w_id = w.w_id
JOIN tpcc_customer c ON c.c_w_id = d.d_w_id AND c.c_d_id = d.d_id
WHERE w.w_id = $WarehouseId AND d.d_id = $DistrictId AND c.c_id = $CustomerId;

\echo 'statement=updateDistrict'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
UPDATE district
SET d_next_o_id = d_next_o_id + 1
WHERE d_w_id = $WarehouseId AND d_id = $DistrictId
RETURNING d_next_o_id - 1;

\echo 'statement=insertOrder'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
INSERT INTO tpcc_orders (
    o_id, o_d_id, o_w_id, o_c_id, o_entry_d, o_carrier_id, o_ol_cnt, o_all_local
) VALUES (
    :order_id, $DistrictId, $WarehouseId, $CustomerId, current_timestamp, NULL, 1, 1
);

\echo 'statement=insertNewOrder'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
INSERT INTO new_order (no_o_id, no_d_id, no_w_id)
VALUES (:order_id, $DistrictId, $WarehouseId);

\echo 'statement=itemStockLookup'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT i.i_price, s.s_quantity
FROM item i
JOIN stock s ON s.s_i_id = i.i_id
WHERE s.s_w_id = $WarehouseId AND i.i_id = $ItemId;

\echo 'statement=updateStock'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
UPDATE stock
SET s_quantity = s_quantity - $Quantity,
    s_ytd = s_ytd + $Quantity,
    s_order_cnt = s_order_cnt + 1
WHERE s_w_id = $WarehouseId
  AND s_i_id = $ItemId
  AND s_quantity >= $Quantity
RETURNING s_quantity;

\echo 'statement=insertOrderLine'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
INSERT INTO order_line (
    ol_o_id, ol_d_id, ol_w_id, ol_number, ol_i_id, ol_supply_w_id,
    ol_delivery_d, ol_quantity, ol_amount, ol_dist_info
) VALUES (
    :order_id, $DistrictId, $WarehouseId, 1, $ItemId, $WarehouseId,
    NULL, $Quantity, 10.00, 'explain-dist-info'
);

ROLLBACK;
"@
    "payment" = @"
\pset pager off
\timing on

SELECT 'payment' AS transaction_name,
       '$DataScale' AS data_scale,
       current_timestamp AS captured_at;

BEGIN;
SELECT set_config('app.transaction_id', 'explain-payment-$timestamp', true);
SELECT set_config('app.change_type', 'payment', true);

\echo 'statement=contextLookup'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT c.c_id
FROM warehouse w
JOIN district d ON d.d_w_id = w.w_id
JOIN tpcc_customer c ON c.c_w_id = d.d_w_id AND c.c_d_id = d.d_id
WHERE w.w_id = $WarehouseId AND d.d_id = $DistrictId AND c.c_id = $CustomerId;

\echo 'statement=updateWarehouse'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
UPDATE warehouse
SET w_ytd = w_ytd + $amount
WHERE w_id = $WarehouseId;

\echo 'statement=updateDistrict'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
UPDATE district
SET d_ytd = d_ytd + $amount
WHERE d_w_id = $WarehouseId AND d_id = $DistrictId;

\echo 'statement=updateCustomer'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
UPDATE tpcc_customer
SET c_balance = c_balance - $amount,
    c_ytd_payment = c_ytd_payment + $amount,
    c_payment_cnt = c_payment_cnt + 1
WHERE c_w_id = $WarehouseId AND c_d_id = $DistrictId AND c_id = $CustomerId
RETURNING c_balance;

\echo 'statement=insertHistory'
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
INSERT INTO history (
    h_id, h_c_id, h_c_d_id, h_c_w_id, h_d_id, h_w_id, h_date, h_amount, h_data
) VALUES (
    -9223372036854775807, $CustomerId, $DistrictId, $WarehouseId,
    $DistrictId, $WarehouseId, current_timestamp, $amount, 'explain payment'
);

ROLLBACK;
"@
}

function Invoke-TpccPlan {
    param(
        [string]$TransactionName,
        [string]$Sql
    )

    $safeName = $TransactionName -replace "-", "_"
    $sqlFile = Join-Path $env:TEMP "tpcc_${safeName}_$timestamp.sql"
    $containerSql = "/tmp/tpcc_${safeName}_$timestamp.sql"
    $outputFile = Join-Path $resolvedOutputDir "tpcc_${safeName}_explain_$($DataScale)_$timestamp.txt"

    try {
        Set-Content -LiteralPath $sqlFile -Value $Sql -Encoding UTF8
        & docker cp $sqlFile "$Container`:$containerSql" | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "docker cp failed for $TransactionName"
        }

        $planOutput = & docker exec $Container psql -v ON_ERROR_STOP=1 -U $User -d $Database -f $containerSql 2>&1
        $planExitCode = $LASTEXITCODE
        $planOutput | Write-Host
        $planOutput | Set-Content -LiteralPath $outputFile -Encoding UTF8
        if ($planExitCode -ne 0) {
            throw "TPC-C EXPLAIN failed for $TransactionName"
        }

        Write-Host "Saved $TransactionName EXPLAIN to $outputFile"
        return (Resolve-Path $outputFile).Path
    }
    finally {
        Remove-Item -LiteralPath $sqlFile -Force -ErrorAction SilentlyContinue
        & docker exec $Container rm -f $containerSql *> $null
    }
}

$outputFiles = @()
foreach ($transactionName in $transactions) {
    $outputFiles += Invoke-TpccPlan -TransactionName $transactionName -Sql $sqlByTransaction[$transactionName]
}

$manifest = [ordered]@{
    capturedAt = (Get-Date).ToString("o")
    dataScale = $DataScale
    transaction = $Transaction
    rollbackOnly = $true
    files = $outputFiles
    parameters = [ordered]@{
        warehouseId = $WarehouseId
        districtId = $DistrictId
        customerId = $CustomerId
        itemId = $ItemId
        quantity = $Quantity
        paymentAmount = $PaymentAmount
    }
}
$manifestFile = Join-Path $resolvedOutputDir "tpcc_explain_$($DataScale)_$timestamp.json"
$manifest | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $manifestFile -Encoding UTF8
Write-Host "TPC-C EXPLAIN manifest saved to $manifestFile"
