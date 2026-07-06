#!/usr/bin/env bash
set -euo pipefail

echo "[TPC CommerceInsight] Running SQL assets from /sql"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<'SQL'
\i /sql/V1__create_tpch_tables.sql
\i /sql/V2__create_tpcc_tables.sql
\i /sql/V3__create_app_tables.sql
\i /sql/V4__add_constraints.sql
\i /sql/V8__triggers.sql
\i /sql/V9__procedures.sql
\i /sql/V10__indexes_baseline.sql
SQL

if [[ "${LOAD_SAMPLE_DATA:-true}" == "true" ]]; then
  echo "[TPC CommerceInsight] Loading development sample data"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<'SQL'
\i /sql/V11__sample_data.sql
SQL
fi

if [[ "${RUN_BASELINE_VALIDATION:-false}" == "true" ]]; then
  echo "[TPC CommerceInsight] Running baseline validation SQL"
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
fi

echo "[TPC CommerceInsight] SQL asset initialization complete"

