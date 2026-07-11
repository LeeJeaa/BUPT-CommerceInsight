-- TPC CommerceInsight
-- V11: minimal sample data for A/B/C local development.
-- This file is not dbgen data and must not be used as formal benchmark data.
--
-- 初始化说明：
-- V11 只预置 TPC-H 基础数据和 TPC-C 前置数据（warehouse/district/tpcc_customer/item/stock）。
-- tpcc_orders、new_order、order_line 表在 V11 执行后为空。
-- 若需完整的 TPC-C 样例事务数据（含订单、订单明细和支付历史），请在 V11 之后执行：
--   psql -d tpc_commerce -f sql/demo_transaction_data.sql
-- 该脚本会持久化一笔 New-Order 和一笔 Payment 演示数据。
-- 触发器验证块（本文件末尾 BEGIN...ROLLBACK）均已回滚，不会污染基础样例数据。

INSERT INTO region (r_regionkey, r_name, r_comment) VALUES
    (0, 'AFRICA', 'sample region'),
    (1, 'AMERICA', 'sample region'),
    (2, 'ASIA', 'sample region');

INSERT INTO nation (n_nationkey, n_name, n_regionkey, n_comment) VALUES
    (0, 'ALGERIA', 0, 'sample nation'),
    (18, 'CHINA', 2, 'sample nation'),
    (19, 'JAPAN', 2, 'sample nation');

INSERT INTO supplier (
    s_suppkey, s_name, s_address, s_nationkey, s_phone, s_acctbal, s_comment
) VALUES
    (1, 'Supplier#000000001', 'Beijing road 1', 18, '18-111-111-1111', 1000.00, 'sample supplier'),
    (2, 'Supplier#000000002', 'Tokyo road 1', 19, '19-222-222-2222', 2000.00, 'sample supplier');

INSERT INTO part (
    p_partkey, p_name, p_mfgr, p_brand, p_type, p_size, p_container, p_retailprice, p_comment
) VALUES
    (1, 'promo green part', 'Manufacturer#1', 'Brand#11', 'PROMO BRUSHED COPPER', 10, 'SM BOX', 100.00, 'sample part'),
    (2, 'standard blue part', 'Manufacturer#2', 'Brand#22', 'STANDARD POLISHED TIN', 20, 'LG BOX', 200.00, 'sample part');

INSERT INTO partsupp (
    ps_partkey, ps_suppkey, ps_availqty, ps_supplycost, ps_comment
) VALUES
    (1, 1, 100, 80.00, 'sample partsupp'),
    (2, 2, 200, 150.00, 'sample partsupp');

INSERT INTO customer (
    c_custkey, c_name, c_address, c_nationkey, c_phone, c_acctbal, c_mktsegment, c_comment
) VALUES
    (1, 'Customer#000000001', 'Customer address 1', 18, '18-333-333-3333', 711.56, 'BUILDING', 'sample customer'),
    (2, 'Customer#000000002', 'Customer address 2', 19, '19-444-444-4444', 121.00, 'AUTOMOBILE', 'sample customer');

INSERT INTO orders (
    o_orderkey, o_custkey, o_orderstatus, o_totalprice, o_orderdate,
    o_orderpriority, o_clerk, o_shippriority, o_comment
) VALUES
    (1, 1, 'F', 1000.00, DATE '1994-03-15', '1-URGENT', 'Clerk#000000001', 0, 'sample order'),
    (2, 2, 'O', 2000.00, DATE '1994-04-10', '5-LOW', 'Clerk#000000002', 0, 'sample order'),
    (3, 1, 'O', 500.00, DATE '1995-09-10', '2-HIGH', 'Clerk#000000003', 0, 'sample order');

INSERT INTO lineitem (
    l_orderkey, l_partkey, l_suppkey, l_linenumber, l_quantity,
    l_extendedprice, l_discount, l_tax, l_returnflag, l_linestatus,
    l_shipdate, l_commitdate, l_receiptdate, l_shipinstruct, l_shipmode, l_comment
) VALUES
    (1, 1, 1, 1, 10.00, 1000.00, 0.05, 0.02, 'A', 'F',
     DATE '1994-06-01', DATE '1994-06-03', DATE '1994-06-05', 'DELIVER IN PERSON', 'MAIL', 'sample lineitem'),
    (2, 2, 2, 1, 20.00, 2000.00, 0.10, 0.04, 'N', 'O',
     DATE '1994-07-01', DATE '1994-07-03', DATE '1994-07-06', 'TAKE BACK RETURN', 'SHIP', 'sample lineitem'),
    (3, 1, 1, 1, 5.00, 500.00, 0.00, 0.02, 'N', 'O',
     DATE '1995-09-15', DATE '1995-09-17', DATE '1995-09-19', 'DELIVER IN PERSON', 'MAIL', 'sample lineitem');

INSERT INTO warehouse (
    w_id, w_name, w_street_1, w_street_2, w_city, w_state, w_zip, w_tax, w_ytd
) VALUES
    (1, 'Warehouse 1', 'Street 1', 'Street 2', 'Beijing', 'BJ', '100000000', 0.0800, 0.00);

INSERT INTO district (
    d_id, d_w_id, d_name, d_street_1, d_street_2, d_city, d_state, d_zip,
    d_tax, d_ytd, d_next_o_id
) VALUES
    (1, 1, 'District 1', 'Street 1', 'Street 2', 'Beijing', 'BJ', '100000000', 0.0500, 0.00, 3001);

INSERT INTO tpcc_customer (
    c_id, c_d_id, c_w_id, c_first, c_middle, c_last, c_credit, c_discount,
    c_balance, c_ytd_payment, c_payment_cnt, c_delivery_cnt, c_data
) VALUES
    (1, 1, 1, 'Alice', 'OE', 'Smith', 'GC', 0.1000, 620.25, 0.00, 0, 0, 'sample tpcc customer');

INSERT INTO item (i_id, i_im_id, i_name, i_price, i_data) VALUES
    (1001, 1, 'Sample Item 1001', 25.10, 'sample item'),
    (1002, 2, 'Sample Item 1002', 15.20, 'sample item');

INSERT INTO stock (
    s_i_id, s_w_id, s_quantity, s_ytd, s_order_cnt, s_remote_cnt, s_data
) VALUES
    (1001, 1, 100, 0, 0, 0, 'sample stock'),
    (1002, 1, 80, 0, 0, 0, 'sample stock');

-- password_hash 为占位符，B 联调前必须替换为真实的 bcrypt/argon2 哈希值。
-- 当前值 'change_me_hash' 不可通过任何密码验证，仅用于表结构和权限字段验证。
INSERT INTO app_user (
    username, password_hash, real_name, email, role, status
) VALUES
    ('admin', 'change_me_hash', 'Admin', 'admin@example.com', 'admin', 'approved');

-- V11 sample transaction verification blocks.
-- They are intentionally rolled back so repeated initialization keeps the sample data stable.

BEGIN;
SELECT set_config('app.transaction_id', 'v11-new-order-rollback', true);
SELECT set_config('app.change_type', 'new_order', true);

UPDATE stock
SET
    s_quantity = s_quantity - 3,
    s_ytd = s_ytd + 3,
    s_order_cnt = s_order_cnt + 1
WHERE s_w_id = 1
  AND s_i_id = 1001
  AND s_quantity >= 3;

SELECT
    warehouse_id,
    item_id,
    old_quantity,
    new_quantity,
    change_quantity,
    change_type,
    related_transaction_id
FROM stock_change_log
WHERE related_transaction_id = 'v11-new-order-rollback';

ROLLBACK;

BEGIN;

UPDATE warehouse
SET w_ytd = w_ytd + 25.00
WHERE w_id = 1;

UPDATE district
SET d_ytd = d_ytd + 25.00
WHERE d_w_id = 1
  AND d_id = 1;

UPDATE tpcc_customer
SET
    c_balance = c_balance - 25.00,
    c_ytd_payment = c_ytd_payment + 25.00,
    c_payment_cnt = c_payment_cnt + 1
WHERE c_w_id = 1
  AND c_d_id = 1
  AND c_id = 1;

INSERT INTO history (
    h_c_id, h_c_d_id, h_c_w_id, h_d_id, h_w_id, h_amount, h_data
) VALUES (
    1, 1, 1, 1, 1, 25.00, 'v11 payment rollback'
);

SELECT
    c_id AS customer_id,
    c_balance AS payment_rollback_balance,
    c_payment_cnt AS payment_rollback_count
FROM tpcc_customer
WHERE c_w_id = 1
  AND c_d_id = 1
  AND c_id = 1;

ROLLBACK;
