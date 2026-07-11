-- TPC CommerceInsight
-- V7: import cleaning rules and SQL templates for system demo import.
-- Web demo import contract: only orders and lineitem are supported tables.
-- partsupp belongs only to the formal dbgen + COPY import and is intentionally
-- not exposed through a PREPARE statement in this Web/API demo contract.

-- Error log template used when one input row fails validation.
PREPARE import_log_error(bigint, bigint, varchar, text, text) AS
INSERT INTO import_error_log (
    task_id, line_number, field_name, field_value, error_reason
) VALUES (
    $1, $2, $3, $4, $5
);

-- Import task update template.
PREPARE import_update_task(bigint, varchar, bigint, bigint, bigint) AS
UPDATE import_task
SET
    status = $2,
    total_rows = $3,
    success_rows = $4,
    failed_rows = $5
WHERE task_id = $1;

-- Clean orders insert template.
-- Required checks before calling:
-- o_orderkey: non-null integer
-- o_custkey: non-null integer and should reference customer
-- o_totalprice: numeric and >= 0
-- o_orderdate: valid yyyy-MM-dd date
-- o_orderstatus: one of O/F/P
PREPARE import_insert_orders(
    integer, integer, varchar, numeric, date, varchar, varchar, integer, varchar
) AS
INSERT INTO orders (
    o_orderkey, o_custkey, o_orderstatus, o_totalprice, o_orderdate,
    o_orderpriority, o_clerk, o_shippriority, o_comment
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8, $9
);

-- Clean lineitem insert template.
-- Required checks before calling:
-- l_orderkey: non-null integer and should reference orders
-- l_quantity: numeric and > 0
-- l_extendedprice: numeric and >= 0
-- l_discount: numeric between 0 and 1
-- l_shipdate/l_commitdate/l_receiptdate: valid yyyy-MM-dd dates
PREPARE import_insert_lineitem(
    integer, integer, integer, integer, numeric, numeric, numeric, numeric,
    varchar, varchar, date, date, date, varchar, varchar, varchar
) AS
INSERT INTO lineitem (
    l_orderkey, l_partkey, l_suppkey, l_linenumber, l_quantity,
    l_extendedprice, l_discount, l_tax, l_returnflag, l_linestatus,
    l_shipdate, l_commitdate, l_receiptdate, l_shipinstruct, l_shipmode, l_comment
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8,
    $9, $10, $11, $12, $13, $14, $15, $16
);

-- ON CONFLICT DO NOTHING: skip duplicate primary keys and let B detect 0 affected rows.
-- B should insert the skipped row into import_error_log with reason 'primary_key_conflict'.
PREPARE import_insert_orders_skip_dup(
    integer, integer, varchar, numeric, date, varchar, varchar, integer, varchar
) AS
INSERT INTO orders (
    o_orderkey, o_custkey, o_orderstatus, o_totalprice, o_orderdate,
    o_orderpriority, o_clerk, o_shippriority, o_comment
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8, $9
)
ON CONFLICT (o_orderkey) DO NOTHING;

PREPARE import_insert_lineitem_skip_dup(
    integer, integer, integer, integer, numeric, numeric, numeric, numeric,
    varchar, varchar, date, date, date, varchar, varchar, varchar
) AS
INSERT INTO lineitem (
    l_orderkey, l_partkey, l_suppkey, l_linenumber, l_quantity,
    l_extendedprice, l_discount, l_tax, l_returnflag, l_linestatus,
    l_shipdate, l_commitdate, l_receiptdate, l_shipinstruct, l_shipmode, l_comment
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8,
    $9, $10, $11, $12, $13, $14, $15, $16
)
ON CONFLICT (l_orderkey, l_linenumber) DO NOTHING;

-- ON CONFLICT DO UPDATE (upsert): overwrite existing row with incoming data.
-- Use only if the task explicitly allows re-import to overwrite.
PREPARE import_upsert_orders(
    integer, integer, varchar, numeric, date, varchar, varchar, integer, varchar
) AS
INSERT INTO orders (
    o_orderkey, o_custkey, o_orderstatus, o_totalprice, o_orderdate,
    o_orderpriority, o_clerk, o_shippriority, o_comment
) VALUES (
    $1, $2, $3, $4, $5, $6, $7, $8, $9
)
ON CONFLICT (o_orderkey) DO UPDATE SET
    o_custkey       = EXCLUDED.o_custkey,
    o_orderstatus   = EXCLUDED.o_orderstatus,
    o_totalprice    = EXCLUDED.o_totalprice,
    o_orderdate     = EXCLUDED.o_orderdate,
    o_orderpriority = EXCLUDED.o_orderpriority,
    o_clerk         = EXCLUDED.o_clerk,
    o_shippriority  = EXCLUDED.o_shippriority,
    o_comment       = EXCLUDED.o_comment;

-- Stable error_reason enum for the Web demo importer:
-- required_field_empty
-- invalid_integer
-- invalid_numeric
-- invalid_date
-- field_out_of_range       -- every field range/CHECK violation
-- foreign_key_not_found    -- every missing referenced key
-- primary_key_conflict     -- every duplicate primary key
-- Do not introduce table-specific variants for the three reasons above.
