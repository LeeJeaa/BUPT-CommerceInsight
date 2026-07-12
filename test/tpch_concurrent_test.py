#!/usr/bin/env python3
"""Independent TPC-H Q1/Q5/Q12/Q14 HTTP concurrency acceptance runner."""

from __future__ import annotations

import argparse
import json
import time
from datetime import datetime
from pathlib import Path
from urllib.parse import urlencode

from performance_common import (
    RequestSpec,
    describe_ingest_response,
    ingest_performance_result,
    now_text,
    resolve_token,
    run_concurrent,
    summarize,
    write_ingest_response,
    write_outputs,
)


def query_parameters(args: argparse.Namespace) -> dict[str, dict[str, str]]:
    return {
        "q1": {"shipDate": args.q1_ship_date},
        "q5": {
            "regionName": args.q5_region,
            "startDate": args.q5_start_date,
            "endDate": args.q5_end_date,
        },
        "q12": {
            "shipMode1": args.q12_ship_mode1,
            "shipMode2": args.q12_ship_mode2,
            "startDate": args.q12_start_date,
            "endDate": args.q12_end_date,
        },
        "q14": {"month": args.q14_month},
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://localhost:8080")
    parser.add_argument("--query", choices=["all", "q1", "q5", "q12", "q14"], default="all")
    parser.add_argument("--threads", type=int, default=4)
    parser.add_argument("--requests", type=int, default=20, help="Requests per selected query.")
    parser.add_argument("--timeout", type=float, default=30)
    parser.add_argument("--token")
    parser.add_argument("--username")
    parser.add_argument("--password")
    parser.add_argument("--scale-factor", default="0.1")
    parser.add_argument("--output-dir", default="report/performance")
    parser.add_argument("--skip-ingest", action="store_true", help="Do not POST results to the backend.")
    parser.add_argument("--q1-ship-date", default="1998-09-02")
    parser.add_argument("--q5-region", default="ASIA")
    parser.add_argument("--q5-start-date", default="1994-01-01")
    parser.add_argument("--q5-end-date", default="1995-01-01")
    parser.add_argument("--q12-ship-mode1", default="MAIL")
    parser.add_argument("--q12-ship-mode2", default="SHIP")
    parser.add_argument("--q12-start-date", default="1994-01-01")
    parser.add_argument("--q12-end-date", default="1995-01-01")
    parser.add_argument("--q14-month", default="1995-09-01")
    args = parser.parse_args()

    base_url = args.base_url.rstrip("/")
    token = resolve_token(
        base_url,
        token=args.token,
        username=args.username,
        password=args.password,
        timeout=args.timeout,
    )
    selected = ["q1", "q5", "q12", "q14"] if args.query == "all" else [args.query]
    parameters = query_parameters(args)
    output_dir = Path(args.output_dir)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    failed = False

    for query_name in selected:
        url = f"{base_url}/api/tpch/{query_name}?{urlencode(parameters[query_name])}"
        spec = RequestSpec(
            name=query_name,
            url=url,
            headers={"Authorization": f"Bearer {token}", "Accept": "application/json"},
        )
        started_at = now_text()
        samples, elapsed = run_concurrent(spec, args.threads, args.requests, args.timeout)
        ended_at = now_text()
        summary = summarize(
            test_name=f"TPC-H {query_name.upper()} Concurrent Query Test SF{args.scale_factor}",
            test_type="tpch",
            operation=query_name,
            scale_factor=args.scale_factor,
            threads=args.threads,
            target_url=url,
            samples=samples,
            elapsed_seconds=elapsed,
            started_at=started_at,
            ended_at=ended_at,
        )
        stem = f"tpch_{query_name}_sf{args.scale_factor}_{timestamp}"
        paths = write_outputs(output_dir, stem, summary, samples)
        print(json.dumps(summary, ensure_ascii=False, indent=2))
        print(f"JSON: {paths['json']}")
        print(f"CSV: {paths['summaryCsv']}")
        if not args.skip_ingest:
            try:
                response = ingest_performance_result(base_url, token, summary, args.timeout)
                response_path = output_dir / f"{stem}_ingest_response.json"
                write_ingest_response(response_path, response)
                print(f"Performance result write succeeded: {describe_ingest_response(response)}")
                print(f"Write response: {response_path}")
            except Exception as exc:
                failed = True
                print(f"Performance result write failed for {query_name}: {exc}")
        failed = failed or summary["failCount"] > 0
        time.sleep(0.1)

    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
