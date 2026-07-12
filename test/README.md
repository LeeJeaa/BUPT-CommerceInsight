# Performance Test Entrypoints

The two acceptance entrypoints are intentionally independent:

- `tpch_concurrent_test.py`: Q1, Q5, Q12, and Q14 HTTP query tests.
- `tpcc_concurrent_test.py`: New-Order and Payment HTTP transaction tests.

Shared request, metric, CSV/JSON, login, and performance-result ingestion logic
lives in `performance_common.py`. `performance_test.py` remains a low-level
single-endpoint diagnostic runner; it is not the final TPC-H/TPC-C acceptance
entrypoint.

## Prerequisites

1. PostgreSQL is running with the target dataset.
2. The backend runs with `dev` or `prod`, not `mock`.
3. An administrator token is supplied, or the script logs in using an
   administrator username and password.

The examples below use the local test account created by the backend in the
`dev` profile.

## TPC-H

Run all four queries with four threads and 20 requests per query:

```powershell
python test/tpch_concurrent_test.py `
  --base-url http://localhost:8080 `
  --query all `
  --threads 4 `
  --requests 20 `
  --scale-factor 0.1 `
  --username admin `
  --password admin123 `
  --timeout 30
```

Use `--query q1`, `q5`, `q12`, or `q14` to run one query. Date and region
parameters are configurable through the query-specific command-line options.
The defaults match standard dbgen SF=0.1 dates.

## TPC-C

Run New-Order and Payment with four threads and 20 requests per transaction:

```powershell
python test/tpcc_concurrent_test.py `
  --base-url http://localhost:8080 `
  --transaction all `
  --threads 4 `
  --requests 20 `
  --scale-factor 0.1 `
  --username admin `
  --password admin123 `
  --timeout 30
```

Supply custom JSON inline or from files:

```powershell
python test/tpcc_concurrent_test.py `
  --transaction new-order `
  --new-order-body-file docs/api/examples/tpcc-new-order.json `
  --token $env:COMMERCE_TOKEN
```

Equivalent options exist for Payment: `--payment-body` and
`--payment-body-file`. Use `--transaction new-order` or `payment` to run only
one transaction type.

## Output Contract

Each selected query or transaction produces:

- `<stem>.json`: summary and per-request samples.
- `<stem>_summary.csv`: one summary row.
- `<stem>_samples.csv`: one row per request.
- `<stem>_ingest_response.json`: response from B's performance-result API.

Summary fields include `testType`, `scaleFactor`, `threadCount`,
`totalRequests`, `successCount`, `failCount`, `avgLatencyMs`, `maxLatencyMs`,
`minLatencyMs`, `p95LatencyMs`, `throughput`, and `qps` or `tps`.

TPC-H always writes `testType=tpch`; TPC-C always writes `testType=tpcc`.
The backend write payload follows its DTO exactly and does not use the legacy
`successRequests` or `failedRequests` names. If the backend returns no result
ID, the complete successful response is printed and archived instead.

Use `--skip-ingest` only for isolated diagnostics. Normal acceptance runs must
write results back to `/api/performance/results`.

## Tests

```powershell
python -m unittest discover -s test -p "test_*.py" -v
python -m py_compile test/performance_common.py test/tpch_concurrent_test.py test/tpcc_concurrent_test.py
```
