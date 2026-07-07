# D Environment And Import Guide

This directory is owned by member D. It contains Docker PostgreSQL setup,
initialization entrypoints, TPC-H data generation/import scripts, and database
verification helpers.

## Start PostgreSQL

```powershell
cd db
docker compose up -d
docker compose ps
```

Default connection:

```text
host: localhost
port: 5432
database: tpc_commerce
user: tpc_admin
password: tpc_password
```

Initialization switches in `db/docker-compose.yml`:

```text
LOAD_BASELINE_INDEXES=true      # set false for no-index EXPLAIN baseline
LOAD_SAMPLE_DATA=false          # set true for B/C local sample validation
RUN_BASELINE_VALIDATION=false   # set true only when sample data is loaded
```

## Reset Database

```powershell
cd db/scripts
.\reset-db.ps1
```

Use `-KeepData` when you want to restart containers without deleting the
PostgreSQL volume.

## SQL Asset Rule

`sql/` is the only source directory for business SQL. `db/init/` only invokes
`/sql/V*.sql` files from Docker initialization scripts.

Development initialization order:

```text
V1 -> V2 -> V3 -> V4 -> V8 -> V9 -> optional V10 -> optional V11
```

Formal import order:

```text
V1 -> V2 -> V3 -> COPY TPC-H data -> V4 -> V8 -> V9 -> optional V10 -> row counts -> EXPLAIN
```

## Generate TPC-H Data

Put dbgen under `tools/tpch-dbgen`, then run:

```powershell
cd db/scripts
.\generate-tpch.ps1 -ScaleFactor 0.1
```

Generated `.tbl` files go under `data/tpch/SF0.1` by default. Do not commit
large generated data files.

The provided course dataset under `资料/tpc-h数据(2)` already contains `*.txt`
files generated at `dbgen -s 0.2` scale. It is suitable for
development, import validation, and feature demos. The guide requires at least
600M total data for performance evaluation, so final performance testing still
needs a larger generated dataset.

Inspect the provided dataset:

```powershell
cd db/scripts
.\inspect-tpch-data.ps1
```

## Load TPC-H Data

Preview COPY commands:

```powershell
cd db/scripts
.\load-tpch.ps1 -ScaleFactor 0.2 -DryRun
```

Run import after data exists:

```powershell
.\load-tpch.ps1 -ScaleFactor 0.2 -FileExtension txt
```

The scripts auto-detect the provided `tpc-h数据(2)` directory to avoid Windows
PowerShell source-encoding issues with Chinese paths.

Logs are written to `report/import_logs/`.

## Count Rows

```powershell
cd db/scripts
.\count-tables.ps1
```

If running from the host with `psql` installed:

```powershell
psql -h localhost -p 5432 -U commerce -d commerce_insight -f db/scripts/count-tables.sql
```

## Verify Local Environment

```powershell
cd db/scripts
.\verify-environment.ps1
```

This checks Docker CLI availability, Docker daemon status, Compose config,
required directories, LF line endings for the init script, and whether A's SQL
assets are already present.

## Smoke Check PostgreSQL

```powershell
cd db/scripts
.\smoke-db.ps1
```

This verifies that the `tpc-commerce-postgres` container accepts SQL
connections.
