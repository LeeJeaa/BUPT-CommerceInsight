# Course Guide Gap Check

## Source Material Reviewed

- `资料/指导书-TPC-Benchmark电商数据管理系统-2026(1)(2).pdf`
- `资料/tpc-h数据(2)/*.txt`

## What Matches Current Work

- PostgreSQL is an allowed and recommended DBMS.
- Docker PostgreSQL setup matches the team document's D-side environment role.
- The project needs TPC-H data import, query plans, index comparison, and concurrent performance metrics; current D-side scripts cover these workflow foundations.
- The provided dataset row counts match the guide's `dbgen -s 0.2` example:
  - `orders.txt`: 300000
  - `supplier.txt`: 2000
  - `region.txt`: 5
  - `nation.txt`: 25
  - `part.txt`: 40000
  - `partsupp.txt`: 160000
  - `customer.txt`: 30000
  - `lineitem.txt`: 1199969

## Corrections Made

- The import script now defaults to `资料/tpc-h数据(2)` and `*.txt` files.
- The script still supports `*.tbl` for future dbgen output.
- Added `inspect-tpch-data.ps1` to verify row counts, column counts, and trailing delimiters.

## New Or Clarified Requirements

- Final performance data should be generated at no less than 600M total scale. The provided dataset is about 200M scale and should be treated as development/demo data.
- Data import is not just raw database loading: the application must support batch import with cleaning, error logs, selectable file paths, and progress reporting.
- At least two imported tables should have at least two cleaned fields each.
- Data export must support at least three database tables and selectable output paths.
- TPC-H statistical analysis must include at least two complex SQL queries, elapsed time recording, and execution plan analysis.
- TPC-C work should include at least two transactions, with timing for whole transactions and internal SQL statements.
- The final system must include user management, batch import, stored procedures/triggers, index design, and graphical query indicators.

## Impact On Member D

- D should keep formal environment, import verification, row-count logs, EXPLAIN artifacts, performance results, and report integration.
- B/C own application-level import UI/API, progress bar, export UI/API, and query presentation, but D's scripts and logs can provide the formal verification baseline.
- A owns schema, constraints, triggers, stored procedures, and index SQL; D verifies that they execute and captures performance evidence.
