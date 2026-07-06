# A Branch Pull Check

## Source

- Branch inspected: `dev/a-database-sql`
- Commit observed: `9b98c04782970fa994c299b1e6bbed1190316af4`
- Local sync scope:
  - `README.md`
  - `docs/`
  - `sql/`

`db/` from A was not copied wholesale because member D already owns the Docker,
import, reset, row-count, and performance scripts. D-side files were adjusted to
match A's database connection contract instead.

## A Deliverables Found

- `sql/V1__create_tpch_tables.sql`
- `sql/V2__create_tpcc_tables.sql`
- `sql/V3__create_app_tables.sql`
- `sql/V4__add_constraints.sql`
- `sql/V5__tpch_queries.sql`
- `sql/V6__tpcc_transaction_sql.sql`
- `sql/V7__import_cleaning_rules.sql`
- `sql/V8__triggers.sql`
- `sql/V9__procedures.sql`
- `sql/V10__indexes_baseline.sql`
- `sql/V11__sample_data.sql`
- `sql/V12__explain_baseline.sql`
- `sql/demo_transaction_data.sql`
- `docs/docs-A/` handoff documents
- `docs/database/dbms-selection.md`

## Validation Results

- Fresh PostgreSQL init with A SQL passed.
- Tables created: 24 public tables, including TPC-H, minimal TPC-C, and app auxiliary tables.
- Core init order passed:
  - `V1 -> V2 -> V3 -> V4 -> V8 -> V9 -> V10`
- A sample validation also passed when `V11`, `V5`, `V6`, and `V7` were run in the expected mode.
- D imported the provided `SF=0.2` course dataset successfully:
  - `region`: 5
  - `nation`: 25
  - `supplier`: 2000
  - `customer`: 30000
  - `part`: 40000
  - `partsupp`: 160000
  - `orders`: 300000
  - `lineitem`: 1199969

## D-Side Changes After Pull

- Docker defaults aligned to A's contract:
  - container: `tpc-commerce-postgres`
  - database: `tpc_commerce`
  - user: `tpc_admin`
  - password: `tpc_password`
- `db/init/00_run_sql_assets.sh` now supports:
  - `LOAD_BASELINE_INDEXES`
  - `LOAD_SAMPLE_DATA`
  - `RUN_BASELINE_VALIDATION`
- Row count script now includes TPC-C setup tables used by A.

## Issue To Feed Back

The provided course dataset has shifted dates:

- `orders.o_orderdate`: 2015-01-01 to 2021-08-02
- `lineitem.l_shipdate`: 2015-01-02 to 2021-12-01
- `lineitem.l_receiptdate`: 2015-01-04 to 2021-12-30

A's current Q1/Q5/Q12/Q14 templates use traditional TPC-H dates such as
1994, 1995, and 1998. On the provided dataset, those filters can return zero
rows. For example:

- `l_shipdate <= DATE '1995-12-31'` returns 0 rows.
- `l_shipdate <= DATE '2020-12-31'` returns 1061853 rows.

Recommendation: A/B should parameterize query dates or update default demo
dates to match the provided 2015-2021 dataset before C builds final charts and
D records final EXPLAIN screenshots.
