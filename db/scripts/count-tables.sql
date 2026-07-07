\pset format aligned
\pset pager off
\timing on

SELECT 'region' AS table_name, COUNT(*) AS row_count FROM region
UNION ALL
SELECT 'nation', COUNT(*) FROM nation
UNION ALL
SELECT 'supplier', COUNT(*) FROM supplier
UNION ALL
SELECT 'customer', COUNT(*) FROM customer
UNION ALL
SELECT 'part', COUNT(*) FROM part
UNION ALL
SELECT 'partsupp', COUNT(*) FROM partsupp
UNION ALL
SELECT 'orders', COUNT(*) FROM orders
UNION ALL
SELECT 'lineitem', COUNT(*) FROM lineitem
UNION ALL
SELECT 'warehouse', COUNT(*) FROM warehouse
UNION ALL
SELECT 'district', COUNT(*) FROM district
UNION ALL
SELECT 'tpcc_customer', COUNT(*) FROM tpcc_customer
UNION ALL
SELECT 'item', COUNT(*) FROM item
UNION ALL
SELECT 'stock', COUNT(*) FROM stock
ORDER BY table_name;
