-- TPC CommerceInsight
-- V12: sample EXPLAIN ANALYZE templates for A/D performance evidence.
-- D should run these on formal SF=0.1/SF=0.6/SF=1 data and save text output under report/explain_plans/.

-- Q1: pricing summary report.
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
WHERE l_shipdate <= DATE '1995-12-31'
GROUP BY
    l_returnflag,
    l_linestatus
ORDER BY
    l_returnflag,
    l_linestatus;

-- Q5: local supplier revenue by nation.
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT
    TRIM(n.n_name) AS nation_name,
    SUM(l.l_extendedprice * (1 - l.l_discount)) AS revenue
FROM customer c
JOIN orders o
    ON c.c_custkey = o.o_custkey
JOIN lineitem l
    ON l.l_orderkey = o.o_orderkey
JOIN supplier s
    ON s.s_suppkey = l.l_suppkey
JOIN nation n
    ON n.n_nationkey = c.c_nationkey
    AND n.n_nationkey = s.s_nationkey
JOIN region r
    ON r.r_regionkey = n.n_regionkey
WHERE r.r_name = 'ASIA'
  AND o.o_orderdate >= DATE '1994-01-01'
  AND o.o_orderdate < DATE '1995-01-01'
GROUP BY
    n.n_name
ORDER BY
    revenue DESC;

-- Q12: shipping mode and order priority.
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT
    TRIM(l.l_shipmode) AS ship_mode,
    SUM(CASE
        WHEN o.o_orderpriority IN ('1-URGENT', '2-HIGH') THEN 1
        ELSE 0
    END) AS high_line_count,
    SUM(CASE
        WHEN o.o_orderpriority NOT IN ('1-URGENT', '2-HIGH') THEN 1
        ELSE 0
    END) AS low_line_count
FROM orders o
JOIN lineitem l
    ON o.o_orderkey = l.l_orderkey
WHERE l.l_shipmode IN ('MAIL', 'SHIP')
  AND l.l_commitdate < l.l_receiptdate
  AND l.l_shipdate < l.l_commitdate
  AND l.l_receiptdate >= DATE '1994-01-01'
  AND l.l_receiptdate < DATE '1995-01-01'
GROUP BY
    TRIM(l.l_shipmode)
ORDER BY
    TRIM(l.l_shipmode);

-- Q14: promotional revenue.
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT
    100.00 * SUM(CASE
        WHEN p.p_type LIKE 'PROMO%' THEN l.l_extendedprice * (1 - l.l_discount)
        ELSE 0
    END) / NULLIF(SUM(l.l_extendedprice * (1 - l.l_discount)), 0) AS promo_revenue_percent
FROM lineitem l
JOIN part p
    ON p.p_partkey = l.l_partkey
WHERE l.l_shipdate >= DATE '1995-09-01'
  AND l.l_shipdate < DATE '1995-10-01';
