#!/usr/bin/env python3
"""Simple concurrent HTTP performance runner for member D."""

from __future__ import annotations

import argparse
import csv
import json
import statistics
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from typing import Any
from urllib import error, request


@dataclass
class Sample:
    ok: bool
    latency_ms: float
    status_code: int | None
    error: str | None = None


def parse_headers(raw_headers: list[str]) -> dict[str, str]:
    headers: dict[str, str] = {}
    for item in raw_headers:
        if ":" not in item:
            raise ValueError(f"Header must be KEY:VALUE, got {item!r}")
        key, value = item.split(":", 1)
        headers[key.strip()] = value.strip()
    return headers


def run_one(
    url: str,
    method: str,
    headers: dict[str, str],
    body: bytes | None,
    timeout: float,
) -> Sample:
    started = time.perf_counter()
    req = request.Request(url=url, method=method, headers=headers, data=body)
    try:
        with request.urlopen(req, timeout=timeout) as resp:
            resp.read()
            latency_ms = (time.perf_counter() - started) * 1000
            return Sample(200 <= resp.status < 400, latency_ms, resp.status)
    except error.HTTPError as exc:
        latency_ms = (time.perf_counter() - started) * 1000
        return Sample(False, latency_ms, exc.code, str(exc))
    except Exception as exc:  # Network failures should be counted, not crash the run.
        latency_ms = (time.perf_counter() - started) * 1000
        return Sample(False, latency_ms, None, str(exc))


def percentile(values: list[float], percent: float) -> float:
    if not values:
        return 0.0
    ordered = sorted(values)
    index = min(len(ordered) - 1, max(0, round((percent / 100) * len(ordered) + 0.5) - 1))
    return ordered[index]


def summarize(args: argparse.Namespace, samples: list[Sample], started_at: str, ended_at: str, elapsed_s: float) -> dict[str, Any]:
    latencies = [sample.latency_ms for sample in samples]
    success_count = sum(1 for sample in samples if sample.ok)
    failed_count = len(samples) - success_count
    return {
        "testName": args.test_name,
        "scaleFactor": args.scale_factor,
        "url": args.url,
        "method": args.method,
        "threadCount": args.threads,
        "totalRequests": args.requests,
        "successCount": success_count,
        "failCount": failed_count,
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
    if output.suffix.lower() == ".csv":
        with output.open("w", newline="", encoding="utf-8") as fp:
            writer = csv.DictWriter(fp, fieldnames=list(summary.keys()))
            writer.writeheader()
            writer.writerow(summary)
        detail_path = output.with_name(output.stem + "_samples.csv")
        with detail_path.open("w", newline="", encoding="utf-8") as fp:
            writer = csv.DictWriter(fp, fieldnames=["ok", "latencyMs", "statusCode", "error"])
            writer.writeheader()
            for sample in samples:
                writer.writerow(
                    {
                        "ok": sample.ok,
                        "latencyMs": round(sample.latency_ms, 3),
                        "statusCode": sample.status_code or "",
                        "error": sample.error or "",
                    }
                )
        return

    payload = {
        "summary": summary,
        "samples": [
            {
                "ok": sample.ok,
                "latencyMs": round(sample.latency_ms, 3),
                "statusCode": sample.status_code,
                "error": sample.error,
            }
            for sample in samples
        ],
    }
    output.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description="Run concurrent HTTP performance tests.")
    parser.add_argument("--url", required=True)
    parser.add_argument("--method", default="GET", choices=["GET", "POST", "PUT", "DELETE"])
    parser.add_argument("--threads", type=int, default=4)
    parser.add_argument("--requests", type=int, default=20)
    parser.add_argument("--test-name", default="smoke")
    parser.add_argument("--scale-factor", default="0.1")
    parser.add_argument("--output", default="report/performance/performance_result.json")
    parser.add_argument("--body", help="Raw request body for POST/PUT.")
    parser.add_argument("--header", action="append", default=[], help="HTTP header as KEY:VALUE. Repeatable.")
    parser.add_argument("--timeout", type=float, default=10)
    args = parser.parse_args()

    if args.threads < 1:
        raise ValueError("--threads must be >= 1")
    if args.requests < 1:
        raise ValueError("--requests must be >= 1")

    headers = parse_headers(args.header)
    body = args.body.encode("utf-8") if args.body is not None else None
    if body and "Content-Type" not in headers:
        headers["Content-Type"] = "application/json"

    started_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    started = time.perf_counter()

    samples: list[Sample] = []
    with ThreadPoolExecutor(max_workers=args.threads) as pool:
        futures = [
            pool.submit(run_one, args.url, args.method, headers, body, args.timeout)
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
