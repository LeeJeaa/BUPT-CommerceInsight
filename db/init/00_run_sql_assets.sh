#!/usr/bin/env bash
set -euo pipefail

echo "[init] running CommerceInsight SQL assets from /sql"

run_sql_asset() {
  local file="$1"
  if [[ -f "$file" ]]; then
    echo "[init] applying $file"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f "$file"
  else
    echo "[init] skip missing $file"
  fi
}

run_sql_asset /sql/V1__create_tpch_tables.sql
run_sql_asset /sql/V2__create_tpcc_tables.sql
run_sql_asset /sql/V3__create_app_tables.sql

if [[ "${DEFER_POST_COPY_ASSETS:-false}" == "true" ]]; then
  echo "[init] defer V4/V8/V9/V10/V11/V13 until the formal COPY load finishes"
else
  run_sql_asset /sql/V4__add_constraints.sql
  run_sql_asset /sql/V8__triggers.sql
  run_sql_asset /sql/V9__procedures.sql

  if [[ "${LOAD_BASELINE_INDEXES:-true}" == "true" ]]; then
    run_sql_asset /sql/V10__indexes_baseline.sql
  else
    echo "[init] skip V10 indexes because LOAD_BASELINE_INDEXES is not true"
  fi

  if [[ "${LOAD_SAMPLE_DATA:-false}" == "true" ]]; then
    run_sql_asset /sql/V11__sample_data.sql
  else
    echo "[init] skip V11 sample data because LOAD_SAMPLE_DATA is not true"
  fi

  # V13 is an idempotent compatibility migration. New volumes apply it as the
  # final schema step; existing volumes use db/scripts/migrate-db.ps1 because
  # docker-entrypoint-initdb.d only runs when PostgreSQL initializes an empty
  # data directory.
  run_sql_asset /sql/V13__migrate_legacy_stock_change_log.sql
fi

if [[ "${RUN_BASELINE_VALIDATION:-false}" == "true" ]]; then
  echo "[init] running baseline validation SQL"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<'SQL'
\i /sql/V5__tpch_queries.sql
\i /sql/V6__tpcc_transaction_sql.sql
\i /sql/V7__import_cleaning_rules.sql
EXECUTE tpch_q1(DATE '1995-12-31');
EXECUTE tpch_q5('ASIA', DATE '1994-01-01', DATE '1995-01-01');
EXECUTE tpch_q12('MAIL', 'SHIP', DATE '1994-01-01', DATE '1995-01-01');
EXECUTE tpch_q14(DATE '1995-09-01');
SELECT * FROM sp_analyze_region_revenue('ASIA', DATE '1994-01-01', DATE '1995-01-01');
SELECT * FROM sp_analyze_shipping_priority('MAIL', 'SHIP', DATE '1994-01-01', DATE '1995-01-01');
SQL
else
  echo "[init] skip baseline validation because RUN_BASELINE_VALIDATION is not true"
fi

echo "[init] SQL assets completed"
