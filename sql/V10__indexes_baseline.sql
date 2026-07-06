-- TPC CommerceInsight
-- V10: baseline indexes for TPC-H queries, business queries, and TPC-C transactions.
-- Do not add unlimited indexes. Each index below has a clear query or transaction target.

-- Q1 GroupAggregate sort key: pre-sorted input avoids explicit Sort node on large lineitem scans.
CREATE INDEX IF NOT EXISTS idx_lineitem_returnflag_linestatus ON lineitem(l_returnflag, l_linestatus);

-- Q5 and business order-revenue date filtering.
CREATE INDEX IF NOT EXISTS idx_orders_orderdate ON orders(o_orderdate);

-- Q5 and customer order lookup.
CREATE INDEX IF NOT EXISTS idx_orders_custkey ON orders(o_custkey);

-- Q1/Q5/Q12/Q14 joins from lineitem to orders.
CREATE INDEX IF NOT EXISTS idx_lineitem_orderkey ON lineitem(l_orderkey);

-- Q1 and Q14 ship date filtering.
CREATE INDEX IF NOT EXISTS idx_lineitem_shipdate ON lineitem(l_shipdate);

-- Q12 ship mode and receipt date filtering.
CREATE INDEX IF NOT EXISTS idx_lineitem_shipmode_receiptdate ON lineitem(l_shipmode, l_receiptdate);

-- Q5 joins from customer to nation.
CREATE INDEX IF NOT EXISTS idx_customer_nationkey ON customer(c_nationkey);

-- Q5 joins from supplier to nation.
CREATE INDEX IF NOT EXISTS idx_supplier_nationkey ON supplier(s_nationkey);

-- Partsupp business query and supplier lookup.
CREATE INDEX IF NOT EXISTS idx_partsupp_suppkey ON partsupp(ps_suppkey);

-- Partsupp business query and part lookup.
CREATE INDEX IF NOT EXISTS idx_partsupp_partkey ON partsupp(ps_partkey);

-- TPC-C transaction log list page and D's transaction test checks.
CREATE INDEX IF NOT EXISTS idx_transaction_log_type_time ON transaction_log(transaction_type, executed_at);

-- TPC-C stock change audit list.
CREATE INDEX IF NOT EXISTS idx_stock_change_log_item_time ON stock_change_log(warehouse_id, item_id, changed_at);
