-- TPC CommerceInsight
-- V9: PostgreSQL routines for reportable database-side analysis.
-- PostgreSQL functions are used here as callable stored routines.

CREATE OR REPLACE FUNCTION sp_analyze_region_revenue(
    p_region_name varchar,
    p_start_date date,
    p_end_date date
)
RETURNS TABLE (
    nation_name varchar,
    revenue numeric
)
LANGUAGE sql
AS $$
    SELECT
        TRIM(n.n_name)::varchar AS nation_name,
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
    WHERE r.r_name = p_region_name
      AND o.o_orderdate >= p_start_date
      AND o.o_orderdate < p_end_date
    GROUP BY
        n.n_name
    ORDER BY
        revenue DESC;
$$;

CREATE OR REPLACE FUNCTION sp_analyze_shipping_priority(
    p_ship_mode1 varchar,
    p_ship_mode2 varchar,
    p_start_date date,
    p_end_date date
)
RETURNS TABLE (
    ship_mode varchar,
    high_line_count bigint,
    low_line_count bigint
)
LANGUAGE sql
AS $$
    SELECT
        TRIM(l.l_shipmode)::varchar AS ship_mode,
        SUM(CASE
            WHEN o.o_orderpriority IN ('1-URGENT', '2-HIGH') THEN 1
            ELSE 0
        END)::bigint AS high_line_count,
        SUM(CASE
            WHEN o.o_orderpriority NOT IN ('1-URGENT', '2-HIGH') THEN 1
            ELSE 0
        END)::bigint AS low_line_count
    FROM orders o
    JOIN lineitem l
        ON o.o_orderkey = l.l_orderkey
    WHERE l.l_shipmode IN (p_ship_mode1, p_ship_mode2)
      AND l.l_commitdate < l.l_receiptdate
      AND l.l_shipdate < l.l_commitdate
      AND l.l_receiptdate >= p_start_date
      AND l.l_receiptdate < p_end_date
    GROUP BY
        TRIM(l.l_shipmode)
    ORDER BY
        TRIM(l.l_shipmode);
$$;

