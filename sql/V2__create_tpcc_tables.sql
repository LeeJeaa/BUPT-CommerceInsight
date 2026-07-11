-- TPC CommerceInsight
-- V2: create minimal TPC-C tables for New-Order and Payment.
-- TPC-C customer/order tables use tpcc_* names to avoid TPC-H conflicts.
-- Frozen decision: this project uses a course-minimal TPC-C implementation,
-- not full BenchmarkSQL-compatible TPC-C.

CREATE TABLE IF NOT EXISTS warehouse (
    w_id integer NOT NULL,
    w_name varchar(16) NOT NULL,
    w_street_1 varchar(32) NOT NULL,
    w_street_2 varchar(32) NOT NULL,
    w_city varchar(32) NOT NULL,
    w_state char(2) NOT NULL,
    w_zip char(9) NOT NULL,
    w_tax numeric(4, 4) NOT NULL,
    w_ytd numeric(12, 2) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS district (
    d_id integer NOT NULL,
    d_w_id integer NOT NULL,
    d_name varchar(16) NOT NULL,
    d_street_1 varchar(32) NOT NULL,
    d_street_2 varchar(32) NOT NULL,
    d_city varchar(32) NOT NULL,
    d_state char(2) NOT NULL,
    d_zip char(9) NOT NULL,
    d_tax numeric(4, 4) NOT NULL,
    d_ytd numeric(12, 2) NOT NULL DEFAULT 0,
    d_next_o_id integer NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS tpcc_customer (
    c_id integer NOT NULL,
    c_d_id integer NOT NULL,
    c_w_id integer NOT NULL,
    c_first varchar(16) NOT NULL,
    c_middle char(2) NOT NULL DEFAULT 'OE',
    c_last varchar(16) NOT NULL,
    c_credit char(2) NOT NULL DEFAULT 'GC',
    c_discount numeric(4, 4) NOT NULL DEFAULT 0,
    c_balance numeric(12, 2) NOT NULL DEFAULT 0,
    c_ytd_payment numeric(12, 2) NOT NULL DEFAULT 0,
    c_payment_cnt integer NOT NULL DEFAULT 0,
    c_delivery_cnt integer NOT NULL DEFAULT 0,
    c_data text
);

CREATE TABLE IF NOT EXISTS history (
    h_id bigserial NOT NULL,
    h_c_id integer NOT NULL,
    h_c_d_id integer NOT NULL,
    h_c_w_id integer NOT NULL,
    h_d_id integer NOT NULL,
    h_w_id integer NOT NULL,
    h_date timestamp NOT NULL DEFAULT current_timestamp,
    h_amount numeric(12, 2) NOT NULL,
    h_data varchar(24) NOT NULL
);

CREATE TABLE IF NOT EXISTS item (
    i_id integer NOT NULL,
    i_im_id integer NOT NULL,
    i_name varchar(24) NOT NULL,
    i_price numeric(5, 2) NOT NULL,
    i_data varchar(50)
);

CREATE TABLE IF NOT EXISTS stock (
    s_i_id integer NOT NULL,
    s_w_id integer NOT NULL,
    s_quantity integer NOT NULL,
    s_ytd integer NOT NULL DEFAULT 0,
    s_order_cnt integer NOT NULL DEFAULT 0,
    s_remote_cnt integer NOT NULL DEFAULT 0,
    s_data varchar(50)
    -- 注意：标准 TPC-C 规范包含 s_dist_01~s_dist_10（每个地区的配送信息字符串）。
    -- 本项目冻结为课程最小实现，有意省略这10列：tpcc_customer 已按最小化实现，district 只有1条样例记录，
    -- ol_dist_info 由 B 调用 tpcc_no_insert_order_line 时作为参数传入（见 V6 注释说明），
    -- 不需要从 stock 表读取。如需完整 BenchmarkSQL 兼容，须在此处补充 s_dist_01~s_dist_10。
);

CREATE TABLE IF NOT EXISTS tpcc_orders (
    o_id integer NOT NULL,
    o_d_id integer NOT NULL,
    o_w_id integer NOT NULL,
    o_c_id integer NOT NULL,
    o_entry_d timestamp NOT NULL DEFAULT current_timestamp,
    o_carrier_id integer,
    o_ol_cnt integer NOT NULL,
    o_all_local integer NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS new_order (
    no_o_id integer NOT NULL,
    no_d_id integer NOT NULL,
    no_w_id integer NOT NULL
);

CREATE TABLE IF NOT EXISTS order_line (
    ol_o_id integer NOT NULL,
    ol_d_id integer NOT NULL,
    ol_w_id integer NOT NULL,
    ol_number integer NOT NULL,
    ol_i_id integer NOT NULL,
    ol_supply_w_id integer NOT NULL,
    ol_delivery_d timestamp,
    ol_quantity numeric(6, 2) NOT NULL,
    ol_amount numeric(12, 2) NOT NULL,
    ol_dist_info char(24) NOT NULL
);
