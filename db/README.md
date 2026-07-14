# Database Deployment, Migration, and Formal Benchmark Guide

`sql/` is the only source of business SQL. `db/init/` only invokes canonical
`/sql/V*.sql` assets and must not contain a second copy of DDL or DML.

## Connection Contract

| Item | Value |
|---|---|
| PostgreSQL | 16 |
| Container | `tpc-commerce-postgres` |
| Host port | `5432` |
| Database | `tpc_commerce` |
| User | `tpc_admin` |
| Password | `tpc_password` |
| JDBC URL | `jdbc:postgresql://localhost:5432/tpc_commerce` |

These values match `backend/src/main/resources/application.yml`.

## New Volume Initialization

```powershell
docker compose -f db/docker-compose.yml up -d
docker compose -f db/docker-compose.yml ps
```

For an empty PostgreSQL volume, `db/init/00_run_sql_assets.sh` applies:

```text
V1 -> V2 -> V3 -> V4 -> V8 -> V9 -> optional V10 -> optional V11 -> V13
```

V13 is applied last as an idempotent compatibility check. Docker runs files in
`/docker-entrypoint-initdb.d` only for an empty data directory, so this path does
not upgrade an already existing volume.

## Existing Volume Migration

Use the non-destructive migration entrypoint for an existing volume:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File db/scripts/migrate-db.ps1
```

The script starts the existing service without deleting its volume, applies
`V13__migrate_legacy_stock_change_log.sql`, reapplies canonical V8 triggers,
and verifies that `change_type` exists, `change_reason` is absent, and the stock
audit trigger is installed. Preview commands without changing the database:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File db/scripts/migrate-db.ps1 -DryRun
```

## Destructive Reset Protection

`reset-db.ps1` does nothing unless `-Force` is present. A destructive reset is:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File db/scripts/reset-db.ps1 -Force
```

Restart containers while preserving the volume:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File db/scripts/reset-db.ps1 -KeepData -Force
```

Read the warning before using either command. The first command deletes the
PostgreSQL volume and all data in it.

## Formal TPC-H COPY Flow

Formal import order differs from development initialization:

```text
V1 -> V2 -> V3 -> COPY -> V4 -> V8 -> V9 -> V13 -> optional V10 -> row counts -> EXPLAIN
```

Generate and import the required SF=0.1 baseline:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File db/scripts/generate-tpch.ps1 -ScaleFactor 0.1
$env:DEFER_POST_COPY_ASSETS = "true"
$env:LOAD_SAMPLE_DATA = "false"
$env:LOAD_BASELINE_INDEXES = "false"
powershell -NoProfile -ExecutionPolicy Bypass -File db/scripts/reset-db.ps1 -Force
powershell -NoProfile -ExecutionPolicy Bypass -File db/scripts/load-tpch.ps1 `
  -ScaleFactor 0.1 -DataDir <absolute-SF0.1-directory> -FileExtension tbl -FinalizeSchema
powershell -NoProfile -ExecutionPolicy Bypass -File db/scripts/count-tables.ps1
```

For formal evidence, pass `-DataScale` to the reset and row-count scripts so
their log names identify the tested dataset, for example
`-DataScale 0.6`. The parameter is an evidence label and does not change data.

`load-tpch.ps1` removes the trailing `|` from dbgen `.tbl` rows before COPY and
loads tables in dependency order. `-FinalizeSchema` applies V4, V8, V9, and V13
after COPY; add `-LoadBaselineIndexes` to apply V10 immediately, or leave it off
when capturing the no-index baseline first. Clear `DEFER_POST_COPY_ASSETS` after
the formal reset. SF=0.6 and SF=1 use the same commands after SF=0.1 succeeds and
machine capacity permits.

Formal TPC-H import intentionally skips V11 because its TPC-H sample rows would
conflict with dbgen primary keys. To add only the course-minimal TPC-C
prerequisites to the combined database, use the dedicated loader:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass `
  -File db/scripts/load-tpcc-prerequisites.ps1
```

The loader reads the canonical V11 asset, extracts only the
`warehouse/district/tpcc_customer/item/stock` block, and refuses a partially
populated target. It does not duplicate the SQL definitions in `db/`.

## EXPLAIN ANALYZE Matrix

The script prepares the requested index state itself. Each run writes separate
Q1, Q5, Q12, and Q14 plans plus a JSON manifest.

TPC-C New-Order and Payment use an independent rollback-only entrypoint. It
executes the same representative reads and writes used by the real service,
captures `EXPLAIN (ANALYZE, BUFFERS)`, and rolls the transaction back so the
acceptance evidence does not alter business rows:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass `
  -File db/scripts/run-tpcc-explain.ps1 `
  -Transaction all `
  -DataScale course-minimal
```

TPC-H scale factors and TPC-C warehouse scale are independent. `-DataScale`
is an evidence label for the TPC-C dataset and must not be presented as a
TPC-H scale factor.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File db/scripts/run-tpch-explain.ps1 `
  -IndexMode without_index -ScaleFactor 0.1

powershell -NoProfile -ExecutionPolicy Bypass -File db/scripts/run-tpch-explain.ps1 `
  -IndexMode with_index -ScaleFactor 0.1
```

Always run `with_index` last so the normal application index state is restored.
Plans are written to `report/explain_plans/`.

## HTTP Performance Tests

Start the backend with the `dev` profile, then run the independent acceptance
entrypoints documented in `test/README.md`. Both scripts write JSON and CSV,
use `successCount` and `failCount`, POST each summary to
`/api/performance/results`, and save the backend write response.

## Verification Helpers

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File db/scripts/verify-environment.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File db/scripts/smoke-db.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File db/scripts/count-tables.ps1
```

Logs are stored under `report/import_logs/`, plans under
`report/explain_plans/`, and concurrency results under `report/performance/`.
