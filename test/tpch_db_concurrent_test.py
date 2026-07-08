#!/usr/bin/env python3
"""Run concurrent TPC-H SQL tests through docker exec psql.

This script is for member D's database-level benchmark evidence. It does not
replace HTTP endpoint testing; it lets us keep moving when the Java backend is
not available on the current machine.
"""

from __future__ import annotations

import argparse
import csv
import json
import statistics
import subprocess
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Any


QUERIES = {
    "q1": """
SELECT COUNT(*) FROM (
  SELECT l_returnflag, l_linestatus, SUM(l_quantity)
  FROM lineitem
  WHERE l_shipdate <= DATE '2020-12-31'
  GROUP BY l_returnflag, l_linestatus
) q;
""",
    "q5": """
SELECT COUNT(*) FROM (
  SELECT n.n_name, SUM(l.l_extendedprice * (1 - l.l_discount))
  FROM customer c
  JOIN orders o ON c.c_custkey = o.o_custkey
  JOIN lineitem l ON l.l_orderkey = o.o_orderkey
  JOIN supplier s ON s.s_suppkey = l.l_suppkey
  JOIN nation n ON n.n_nationkey = c.c_nationkey AND n.n_nationkey = s.s_nationkey
  JOIN region r ON r.r_regionkey = n.n_regionkey
  WHERE r.r_name = 'AFRICA'
    AND o.o_orderdate >= DATE '2020-01-01'
    AND o.o_orderdate < DATE '2021-01-01'
  GROUP BY n.n_name
) q;
""",
    "q12": """
SELECT COUNT(*) FROM (
  SELECT TRIM(l.l_shipmode)
  FROM orders o
  JOIN lineitem l ON o.o_orderkey = l.l_orderkey
  WHERE l.l_shipmode IN ('MAIL', 'SHIP')
    AND l.l_commitdate < l.l_receiptdate
    AND l.l_shipdate < l.l_commitdate
    AND l.l_receiptdate >= DATE '2020-01-01'
    AND l.l_receiptdate < DATE '2021-01-01'
  GROUP BY TRIM(l.l_shipmode)
) q;
""",
    "q14": """
SELECT
  100.00 * SUM(CASE WHEN p.p_type LIKE 'PROMO%' THEN l.l_extendedprice * (1 - l.l_discount) ELSE 0 END)
  / NULLIF(SUM(l.l_extendedprice * (1 - l.l_discount)), 0)
FROM lineitem l
JOIN part p ON p.p_partkey = l.l_partkey
WHERE l.l_shipdate >= DATE '2020-09-01'
  AND l.l_shipdate < DATE '2020-10-01';
""",
}


@dataclass
class Sample:
    ok: bool
    latency_ms: float
    error: str = ""


def run_query(container: str, database: str, user: str, sql: str, timeout: float) -> Sample:
    started = time.perf_counter()
    command = [
        "docker",
        "exec",
        container,
        "psql",
        "-v",
        "ON_ERROR_STOP=1",
        "-U",
        user,
        "-d",
        database,
        "-qAt",
        "-c",
        sql,
    ]
    try:
        completed = subprocess.run(command, capture_output=True, text=True, timeout=timeout, check=False)
        latency_ms = (time.perf_counter() - started) * 1000
        if completed.returncode == 0:
            return Sample(True, latency_ms)
        return Sample(False, latency_ms, completed.stderr.strip() or completed.stdout.strip())
    except Exception as exc:
        latency_ms = (time.perf_counter() - started) * 1000
        return Sample(False, latency_ms, str(exc))


def percentile(values: list[float], percent: float) -> float:
    if not values:
        return 0.0
    ordered = sorted(values)
    index = min(len(ordered) - 1, max(0, round((percent / 100) * len(ordered) + 0.5) - 1))
    return ordered[index]


def summarize(args: argparse.Namespace, samples: list[Sample], started_at: str, ended_at: str, elapsed_s: float) -> dict[str, Any]:
    latencies = [sample.latency_ms for sample in samples]
    success_count = sum(1 for sample in samples if sample.ok)
    fail_count = len(samples) - success_count
    return {
        "testName": args.test_name,
        "queryName": args.query,
        "scaleFactor": args.scale_factor,
        "threadCount": args.threads,
        "totalRequests": args.requests,
        "successCount": success_count,
        "failCount": fail_count,
        "avgLatencyMs": round(statistics.fmean(latencies), 3) if latencies else 0,
        "p95LatencyMs": round(percentile(latencies, 95), 3),
        "minLatencyMs": round(min(latencies), 3) if latencies else 0,
        "maxLatencyMs": round(max(latencies), 3) if latencies else 0,
        "throughput": round(len(samples) / elapsed_s, 3) if elapsed_s > 0 else 0,
        "startedAt": started_at,
        "endedAt": ended_at,
        "elapsedSeconds": round(elapsed_s, 3),
    }


def write_outputs(output: Path, summary: dict[str, Any], samples: list[Sample]) -> None:
    output.parent.mkdir(parents=True, exist_ok=True)
    payload = {
        "summary": summary,
        "samples": [
            {"ok": sample.ok, "latencyMs": round(sample.latency_ms, 3), "error": sample.error}
            for sample in samples
        ],
    }
    output.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")

    csv_path = output.with_suffix(".csv")
    with csv_path.open("w", newline="", encoding="utf-8") as fp:
        writer = csv.DictWriter(fp, fieldnames=list(summary.keys()))
        writer.writeheader()
        writer.writerow(summary)


def main() -> int:
    parser = argparse.ArgumentParser(description="Run concurrent TPC-H SQL tests through Docker PostgreSQL.")
    parser.add_argument("--query", choices=sorted(QUERIES), default="q14")
    parser.add_argument("--threads", type=int, default=4)
    parser.add_argument("--requests", type=int, default=20)
    parser.add_argument("--test-name", default="tpch_db_concurrent")
    parser.add_argument("--scale-factor", default="0.2")
    parser.add_argument("--container", default="tpc-commerce-postgres")
    parser.add_argument("--database", default="tpc_commerce")
    parser.add_argument("--user", default="tpc_admin")
    parser.add_argument("--timeout", type=float, default=30)
    parser.add_argument("--output", default="report/performance/tpch_db_concurrent_q14_sf02.json")
    args = parser.parse_args()

    if args.threads < 1:
        raise ValueError("--threads must be >= 1")
    if args.requests < 1:
        raise ValueError("--requests must be >= 1")

    started_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    started = time.perf_counter()
    samples: list[Sample] = []
    with ThreadPoolExecutor(max_workers=args.threads) as pool:
        futures = [
            pool.submit(run_query, args.container, args.database, args.user, QUERIES[args.query], args.timeout)
            for _ in range(args.requests)
        ]
        for future in as_completed(futures):
            samples.append(future.result())

    elapsed_s = time.perf_counter() - started
    ended_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    summary = summarize(args, samples, started_at, ended_at, elapsed_s)
    write_outputs(Path(args.output), summary, samples)
    print(json.dumps(summary, ensure_ascii=False, indent=2))
    return 0 if summary["failCount"] == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
