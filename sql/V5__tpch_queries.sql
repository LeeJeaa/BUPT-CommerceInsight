-- TPC CommerceInsight
-- V5: TPC-H Q1/Q5/Q12/Q14 query assets.
-- These PREPARE statements are for local PostgreSQL validation.
-- B can copy the SELECT bodies into MyBatis/JDBC and map snake_case aliases to lowerCamelCase API fields.

PREPARE tpch_q1(date) AS
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
WHERE l_shipdate <= $1
GROUP BY
    l_returnflag,
    l_linestatus
ORDER BY
    l_returnflag,
    l_linestatus;

-- API mapping:
-- return_flag -> returnFlag
-- line_status -> lineStatus
-- sum_quantity -> sumQuantity
-- sum_base_price -> sumBasePrice
-- sum_discounted_price -> sumDiscountedPrice
-- sum_charge -> sumCharge
-- avg_quantity -> avgQuantity
-- avg_price -> avgPrice
-- avg_disc -> avgDisc
-- count_order -> countOrder

PREPARE tpch_q5(varchar, date, date) AS
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
WHERE r.r_name = $1
  AND o.o_orderdate >= $2
  AND o.o_orderdate < $3
GROUP BY
    n.n_name
ORDER BY
    revenue DESC;

-- API mapping:
-- nation_name -> nationName
-- revenue -> revenue

PREPARE tpch_q12(varchar, varchar, date, date) AS
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
WHERE l.l_shipmode IN ($1, $2)
  AND l.l_commitdate < l.l_receiptdate
  AND l.l_shipdate < l.l_commitdate
  AND l.l_receiptdate >= $3
  AND l.l_receiptdate < $4
GROUP BY
    TRIM(l.l_shipmode)
ORDER BY
    TRIM(l.l_shipmode);

-- API mapping:
-- ship_mode -> shipMode
-- high_line_count -> highLineCount
-- low_line_count -> lowLineCount

PREPARE tpch_q14(date) AS
SELECT
    100.00 * SUM(CASE
        WHEN p.p_type LIKE 'PROMO%' THEN l.l_extendedprice * (1 - l.l_discount)
        ELSE 0
    END) / NULLIF(SUM(l.l_extendedprice * (1 - l.l_discount)), 0) AS promo_revenue_percent
FROM lineitem l
JOIN part p
    ON p.p_partkey = l.l_partkey
WHERE l.l_shipdate >= $1
  AND l.l_shipdate < ($1 + INTERVAL '1 month')::date;

-- API mapping:
-- promo_revenue_percent -> promoRevenuePercent
