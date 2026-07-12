#!/usr/bin/env python3
"""Independent TPC-C New-Order/Payment HTTP concurrency acceptance runner."""

from __future__ import annotations

import argparse
import json
import time
from datetime import datetime
from pathlib import Path

from performance_common import (
    RequestSpec,
    describe_ingest_response,
    ingest_performance_result,
    load_json_object,
    now_text,
    resolve_token,
    run_concurrent,
    summarize,
    write_ingest_response,
    write_outputs,
)


DEFAULT_NEW_ORDER = {
    "warehouseId": 1,
    "districtId": 1,
    "customerId": 1,
    "items": [{"itemId": 1001, "quantity": 1}],
}
DEFAULT_PAYMENT = {
    "warehouseId": 1,
    "districtId": 1,
    "customerId": 1,
    "paymentAmount": 1.00,
}


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://localhost:8080")
    parser.add_argument("--transaction", choices=["all", "new-order", "payment"], default="all")
    parser.add_argument("--threads", type=int, default=4)
    parser.add_argument("--requests", type=int, default=20, help="Requests per selected transaction.")
    parser.add_argument("--timeout", type=float, default=30)
    parser.add_argument("--token")
    parser.add_argument("--username")
    parser.add_argument("--password")
    parser.add_argument("--scale-factor", default="0.1")
    parser.add_argument("--output-dir", default="report/performance")
    parser.add_argument("--skip-ingest", action="store_true", help="Do not POST results to the backend.")
    parser.add_argument("--new-order-body", help="Inline New-Order JSON object.")
    parser.add_argument("--new-order-body-file", help="Path to a New-Order JSON file.")
    parser.add_argument("--payment-body", help="Inline Payment JSON object.")
    parser.add_argument("--payment-body-file", help="Path to a Payment JSON file.")
    args = parser.parse_args()

    base_url = args.base_url.rstrip("/")
    token = resolve_token(
        base_url,
        token=args.token,
        username=args.username,
        password=args.password,
        timeout=args.timeout,
    )
    bodies = {
        "new-order": load_json_object(args.new_order_body, args.new_order_body_file, DEFAULT_NEW_ORDER),
        "payment": load_json_object(args.payment_body, args.payment_body_file, DEFAULT_PAYMENT),
    }
    selected = ["new-order", "payment"] if args.transaction == "all" else [args.transaction]
    output_dir = Path(args.output_dir)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    failed = False

    for transaction_name in selected:
        url = f"{base_url}/api/tpcc/{transaction_name}"
        body = json.dumps(bodies[transaction_name], ensure_ascii=False).encode("utf-8")
        spec = RequestSpec(
            name=transaction_name,
            url=url,
            method="POST",
            headers={
                "Authorization": f"Bearer {token}",
                "Content-Type": "application/json",
                "Accept": "application/json",
            },
            body=body,
        )
        started_at = now_text()
        samples, elapsed = run_concurrent(spec, args.threads, args.requests, args.timeout)
        ended_at = now_text()
        summary = summarize(
            test_name=f"TPC-C {transaction_name} Concurrent Transaction Test SF{args.scale_factor}",
            test_type="tpcc",
            operation=transaction_name,
            scale_factor=args.scale_factor,
            threads=args.threads,
            target_url=url,
            samples=samples,
            elapsed_seconds=elapsed,
            started_at=started_at,
            ended_at=ended_at,
        )
        stem = f"tpcc_{transaction_name}_sf{args.scale_factor}_{timestamp}"
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
                print(f"Performance result write failed for {transaction_name}: {exc}")
        failed = failed or summary["failCount"] > 0
        time.sleep(0.1)

    return 1 if failed else 0


if __name__ == "__main__":
    raise SystemExit(main())
