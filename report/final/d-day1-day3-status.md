# Member D Day 1-3 Status

## Completed Before A/B/C Delivery

- Created Docker PostgreSQL configuration under `db/docker-compose.yml`.
- Created Docker init entrypoint under `db/init/00_run_sql_assets.sh`.
- Kept `sql/` as the only business SQL source directory.
- Created reset, dbgen, COPY import, and row-count scripts under `db/scripts/`.
- Created Python concurrent HTTP performance runner under `test/performance_test.py`.
- Created import log, EXPLAIN, performance, screenshot, and final report directories.
- Created mock performance JSON/CSV for C's chart development.
- Created EXPLAIN result templates for Q1/Q5/Q12/Q14 with and without indexes.
- Started Docker Desktop daemon from the local machine.
- Pulled and started PostgreSQL 16 Docker container.
- Verified PostgreSQL health status and `select version()` connection.
- Read the course guide PDF and inspected the provided `资料/tpc-h数据(2)` dataset.
- Confirmed the provided dataset has the expected `dbgen -s 0.2` row counts and no trailing pipe delimiter.
- Updated COPY import scripts to support the provided `*.txt` files.
- Pulled A's `dev/a-database-sql` snapshot into local `README.md`, `docs/`, and `sql/`.
- Verified A's SQL assets can initialize a fresh PostgreSQL database and pass sample query validation.
- Aligned D's Docker defaults with A's database connection contract: `tpc-commerce-postgres`, `tpc_admin`, `tpc_commerce`.

## Current Blockers

- A has not delivered `sql/V*.sql`, so formal schema initialization cannot be tested yet.
- B has not delivered backend endpoints, so real API performance testing cannot be run yet.
- The provided dataset is `SF=0.2`, about 200M scale; the guide asks for at least 600M total data for performance evaluation.

## Next Actions After Inputs Arrive

- If the container is stopped, run `docker compose -f db/docker-compose.yml up -d`.
- Put A's SQL files under `sql/`, then run `db/scripts/reset-db.ps1`.
- Use the provided `资料/tpc-h数据(2)` dataset for development import checks.
- Generate a larger TPC-H dataset for final performance testing.
- Load TPC-H data with `db/scripts/load-tpch.ps1`.
- Run row counts with `db/scripts/count-tables.ps1`.
- Run B's endpoints through `test/performance_test.py`.
