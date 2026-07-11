-- TPC CommerceInsight
-- V6: SQL assets for TPC-C New-Order and Payment transaction flows.
-- These PREPARE statements are validation and handoff templates for B.
-- B should execute the same logical steps inside Spring transactions.
-- Frozen decision: course-minimal TPC-C. stock.s_dist_01~s_dist_10 are omitted,
-- so B supplies ol_dist_info when inserting order_line.

-- New-Order step 1: validate warehouse, district, and customer, and read tax/discount.
PREPARE tpcc_no_get_context(integer, integer, integer) AS
SELECT
    w.w_id AS warehouse_id,
    d.d_id AS district_id,
    c.c_id AS customer_id,
    w.w_tax AS warehouse_tax,
    d.d_tax AS district_tax,
    c.c_discount AS customer_discount,
    c.c_credit AS customer_credit,
    d.d_next_o_id AS next_order_id
FROM warehouse w
JOIN district d
    ON d.d_w_id = w.w_id
JOIN tpcc_customer c
    ON c.c_w_id = d.d_w_id
    AND c.c_d_id = d.d_id
WHERE w.w_id = $1
  AND d.d_id = $2
  AND c.c_id = $3;

-- New-Order step 2: atomically get and increment district next order id.
-- B should run this inside the same transaction.
PREPARE tpcc_no_increment_next_order_id(integer, integer) AS
UPDATE district
SET d_next_o_id = d_next_o_id + 1
WHERE d_w_id = $1
  AND d_id = $2
RETURNING d_next_o_id - 1 AS order_id;

-- New-Order step 3: insert order header.
PREPARE tpcc_no_insert_order(integer, integer, integer, integer, integer, integer) AS
INSERT INTO tpcc_orders (
    o_id, o_d_id, o_w_id, o_c_id, o_entry_d, o_carrier_id, o_ol_cnt, o_all_local
) VALUES (
    $3, $2, $1, $4, current_timestamp, NULL, $5, $6
);

-- New-Order step 4: insert new_order marker.
PREPARE tpcc_no_insert_new_order(integer, integer, integer) AS
INSERT INTO new_order (
    no_o_id, no_d_id, no_w_id
) VALUES (
    $3, $2, $1
);

-- New-Order step 5: validate item and stock before stock deduction.
PREPARE tpcc_no_get_item_stock(integer, integer) AS
SELECT
    i.i_id AS item_id,
    i.i_name AS item_name,
    i.i_price AS item_price,
    s.s_w_id AS warehouse_id,
    s.s_quantity AS stock_quantity
FROM item i
JOIN stock s
    ON s.s_i_id = i.i_id
WHERE s.s_w_id = $1
  AND i.i_id = $2;

-- New-Order step 6: deduct stock and update counters.
-- Before calling this, B may set:
-- SELECT set_config('app.transaction_id', :transactionId, true);
-- SELECT set_config('app.change_type', 'new_order', true);
-- Then trg_stock_quantity_change can write stock_change_log automatically.
PREPARE tpcc_no_update_stock(integer, integer, integer) AS
UPDATE stock
SET
    s_quantity = s_quantity - $3,
    s_ytd = s_ytd + $3,
    s_order_cnt = s_order_cnt + 1
WHERE s_w_id = $1
  AND s_i_id = $2
  AND s_quantity >= $3
RETURNING
    s_i_id AS item_id,
    s_w_id AS warehouse_id,
    s_quantity AS new_quantity;

-- New-Order step 7: insert order line.
-- $1=w_id, $2=d_id, $3=o_id, $4=ol_number, $5=i_id, $6=supply_w_id, $7=quantity, $8=amount, $9=dist_info
-- ol_dist_info: 本项目对 TPC-C 做了最小化实现，stock 表未保留 s_dist_01~s_dist_10 字段。
-- B 调用时统一传入固定字符串（如 'dist-info-01'），或按地区编号生成，不影响事务完整性演示。
PREPARE tpcc_no_insert_order_line(integer, integer, integer, integer, integer, integer, numeric, numeric, varchar) AS
INSERT INTO order_line (
    ol_o_id, ol_d_id, ol_w_id, ol_number, ol_i_id, ol_supply_w_id,
    ol_delivery_d, ol_quantity, ol_amount, ol_dist_info
) VALUES (
    $3, $2, $1, $4, $5, $6, NULL, $7, $8, $9
);

-- Common transaction log template.
PREPARE tpcc_insert_transaction_log(varchar, varchar, varchar, bigint, text, bigint) AS
INSERT INTO transaction_log (
    transaction_id, transaction_type, status, elapsed_ms, error_message, executed_by
) VALUES (
    $1, $2, $3, $4, $5, $6
);

-- Payment step 1: validate warehouse, district, and customer.
PREPARE tpcc_pay_get_context(integer, integer, integer) AS
SELECT
    w.w_id AS warehouse_id,
    d.d_id AS district_id,
    c.c_id AS customer_id,
    c.c_balance AS current_balance,
    c.c_ytd_payment AS current_ytd_payment,
    c.c_payment_cnt AS current_payment_count
FROM warehouse w
JOIN district d
    ON d.d_w_id = w.w_id
JOIN tpcc_customer c
    ON c.c_w_id = d.d_w_id
    AND c.c_d_id = d.d_id
WHERE w.w_id = $1
  AND d.d_id = $2
  AND c.c_id = $3;

-- Payment step 2: update warehouse year-to-date amount.
PREPARE tpcc_pay_update_warehouse(integer, numeric) AS
UPDATE warehouse
SET w_ytd = w_ytd + $2
WHERE w_id = $1;

-- Payment step 3: update district year-to-date amount.
PREPARE tpcc_pay_update_district(integer, integer, numeric) AS
UPDATE district
SET d_ytd = d_ytd + $3
WHERE d_w_id = $1
  AND d_id = $2;

-- Payment step 4: update customer balance and payment counters.
PREPARE tpcc_pay_update_customer(integer, integer, integer, numeric) AS
UPDATE tpcc_customer
SET
    c_balance = c_balance - $4,
    c_ytd_payment = c_ytd_payment + $4,
    c_payment_cnt = c_payment_cnt + 1
WHERE c_w_id = $1
  AND c_d_id = $2
  AND c_id = $3
RETURNING c_id AS customer_id, c_balance AS new_balance;

-- Payment step 5: insert history row.
PREPARE tpcc_pay_insert_history(integer, integer, integer, integer, integer, numeric, varchar) AS
INSERT INTO history (
    h_c_id, h_c_d_id, h_c_w_id, h_d_id, h_w_id, h_date, h_amount, h_data
) VALUES (
    $3, $2, $1, $5, $4, current_timestamp, $6, $7
);

/*
Complete New-Order commit example for V11 sample data.
Run after V1 -> V2 -> V3 -> V4 -> V8 -> V11 -> V6.

BEGIN;
SELECT set_config('app.transaction_id', 'no-demo-commit-3001', true);
SELECT set_config('app.change_type', 'new_order', true);

EXECUTE tpcc_no_get_context(1, 1, 1);
EXECUTE tpcc_no_increment_next_order_id(1, 1);
EXECUTE tpcc_no_insert_order(1, 1, 3001, 1, 1, 1);
EXECUTE tpcc_no_insert_new_order(1, 1, 3001);
EXECUTE tpcc_no_get_item_stock(1, 1001);
EXECUTE tpcc_no_update_stock(1, 1001, 2);
EXECUTE tpcc_no_insert_order_line(1, 1, 3001, 1, 1001, 1, 2, 50.20, 'dist-info-01');
EXECUTE tpcc_insert_transaction_log('no-demo-commit-3001', 'new_order', 'committed', 0, NULL, 1);

COMMIT;

Rollback example for stock shortage or validation failure.

BEGIN;
SELECT set_config('app.transaction_id', 'no-demo-rollback', true);
SELECT set_config('app.change_type', 'new_order', true);

EXECUTE tpcc_no_get_context(1, 1, 1);
EXECUTE tpcc_no_get_item_stock(1, 1001);
EXECUTE tpcc_no_update_stock(1, 1001, 100000);

ROLLBACK;
*/

/*
Complete Payment commit example for V11 sample data.
Run after V1 -> V2 -> V3 -> V4 -> V11 -> V6.

BEGIN;

EXECUTE tpcc_pay_get_context(1, 1, 1);
EXECUTE tpcc_pay_update_warehouse(1, 25.00);
EXECUTE tpcc_pay_update_district(1, 1, 25.00);
EXECUTE tpcc_pay_update_customer(1, 1, 1, 25.00);
EXECUTE tpcc_pay_insert_history(1, 1, 1, 1, 1, 25.00, 'payment demo');
EXECUTE tpcc_insert_transaction_log('pay-demo-commit', 'payment', 'committed', 0, NULL, 1);

COMMIT;

Rollback example for invalid amount or validation failure.

BEGIN;

EXECUTE tpcc_pay_get_context(1, 1, 1);
EXECUTE tpcc_pay_update_warehouse(1, -25.00);

ROLLBACK;
*/
