#!/usr/bin/env python3
"""Shared HTTP concurrency, metrics, archival, and result-ingestion helpers."""

from __future__ import annotations

import csv
import json
import os
import re
import statistics
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import asdict, dataclass
from datetime import datetime
from pathlib import Path
from typing import Any
from urllib import error, request


@dataclass(frozen=True)
class RequestSpec:
    name: str
    url: str
    method: str = "GET"
    headers: dict[str, str] | None = None
    body: bytes | None = None


@dataclass(frozen=True)
class Sample:
    operation: str
    ok: bool
    latency_ms: float
    status_code: int | None
    error: str = ""


def now_text() -> str:
    return datetime.now().astimezone().isoformat(timespec="seconds")


def run_one(spec: RequestSpec, timeout: float) -> Sample:
    started = time.perf_counter()
    req = request.Request(
        url=spec.url,
        method=spec.method,
        headers=spec.headers or {},
        data=spec.body,
    )
    try:
        with request.urlopen(req, timeout=timeout) as response:
            raw = response.read()
            latency_ms = (time.perf_counter() - started) * 1000
            ok = 200 <= response.status < 400
            api_error = ""
            if raw:
                try:
                    payload = json.loads(raw.decode("utf-8"))
                    if isinstance(payload, dict) and "code" in payload and payload["code"] != 200:
                        ok = False
                        api_error = str(payload.get("message") or f"API code {payload['code']}")
                except (UnicodeDecodeError, json.JSONDecodeError):
                    pass
            return Sample(spec.name, ok, latency_ms, response.status, api_error)
    except error.HTTPError as exc:
        latency_ms = (time.perf_counter() - started) * 1000
        details = exc.read().decode("utf-8", errors="replace")
        return Sample(spec.name, False, latency_ms, exc.code, details or str(exc))
    except Exception as exc:  # Network errors are measured and reported as failed requests.
        latency_ms = (time.perf_counter() - started) * 1000
        return Sample(spec.name, False, latency_ms, None, str(exc))


def run_concurrent(spec: RequestSpec, threads: int, total_requests: int, timeout: float) -> tuple[list[Sample], float]:
    if threads < 1:
        raise ValueError("threads must be >= 1")
    if total_requests < 1:
        raise ValueError("total_requests must be >= 1")
    if timeout <= 0:
        raise ValueError("timeout must be > 0")

    started = time.perf_counter()
    samples: list[Sample] = []
    with ThreadPoolExecutor(max_workers=threads) as pool:
        futures = [pool.submit(run_one, spec, timeout) for _ in range(total_requests)]
        for future in as_completed(futures):
            samples.append(future.result())
    return samples, time.perf_counter() - started


def percentile(values: list[float], percent: float) -> float:
    if not values:
        return 0.0
    ordered = sorted(values)
    rank = (len(ordered) - 1) * percent / 100
    lower = int(rank)
    upper = min(lower + 1, len(ordered) - 1)
    fraction = rank - lower
    return ordered[lower] + (ordered[upper] - ordered[lower]) * fraction


def summarize(
    *,
    test_name: str,
    test_type: str,
    operation: str,
    scale_factor: str,
    threads: int,
    target_url: str,
    samples: list[Sample],
    elapsed_seconds: float,
    started_at: str,
    ended_at: str,
) -> dict[str, Any]:
    if test_type not in {"tpch", "tpcc"}:
        raise ValueError("test_type must be tpch or tpcc")
    latencies = [sample.latency_ms for sample in samples]
    success_count = sum(sample.ok for sample in samples)
    throughput = len(samples) / elapsed_seconds if elapsed_seconds > 0 else 0.0
    summary: dict[str, Any] = {
        "testName": test_name,
        "testType": test_type,
        "operation": operation,
        "scaleFactor": scale_factor,
        "threadCount": threads,
        "totalRequests": len(samples),
        "successCount": success_count,
        "failCount": len(samples) - success_count,
        "avgLatencyMs": round(statistics.fmean(latencies), 3) if latencies else 0.0,
        "p95LatencyMs": round(percentile(latencies, 95), 3),
        "maxLatencyMs": round(max(latencies), 3) if latencies else 0.0,
        "minLatencyMs": round(min(latencies), 3) if latencies else 0.0,
        "throughput": round(throughput, 3),
        "startedAt": started_at,
        "endedAt": ended_at,
        "elapsedSeconds": round(elapsed_seconds, 3),
        "targetUrl": target_url,
    }
    summary["qps" if test_type == "tpch" else "tps"] = summary["throughput"]
    return summary


def safe_stem(value: str) -> str:
    return re.sub(r"[^a-zA-Z0-9_.-]+", "_", value).strip("_") or "performance"


def write_outputs(
    output_dir: Path,
    stem: str,
    summary: dict[str, Any],
    samples: list[Sample],
) -> dict[str, Path]:
    output_dir.mkdir(parents=True, exist_ok=True)
    normalized_stem = safe_stem(stem)
    json_path = output_dir / f"{normalized_stem}.json"
    summary_csv_path = output_dir / f"{normalized_stem}_summary.csv"
    samples_csv_path = output_dir / f"{normalized_stem}_samples.csv"

    json_path.write_text(
        json.dumps(
            {
                "summary": summary,
                "samples": [
                    {
                        **asdict(sample),
                        "latency_ms": round(sample.latency_ms, 3),
                    }
                    for sample in samples
                ],
            },
            ensure_ascii=False,
            indent=2,
        ),
        encoding="utf-8",
    )
    with summary_csv_path.open("w", newline="", encoding="utf-8-sig") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(summary.keys()))
        writer.writeheader()
        writer.writerow(summary)
    with samples_csv_path.open("w", newline="", encoding="utf-8-sig") as handle:
        writer = csv.DictWriter(
            handle,
            fieldnames=["operation", "ok", "latencyMs", "statusCode", "error"],
        )
        writer.writeheader()
        for sample in samples:
            writer.writerow(
                {
                    "operation": sample.operation,
                    "ok": sample.ok,
                    "latencyMs": round(sample.latency_ms, 3),
                    "statusCode": sample.status_code if sample.status_code is not None else "",
                    "error": sample.error,
                }
            )
    return {"json": json_path, "summaryCsv": summary_csv_path, "samplesCsv": samples_csv_path}


def call_json(
    url: str,
    *,
    method: str,
    payload: dict[str, Any] | None = None,
    token: str | None = None,
    timeout: float = 10,
) -> dict[str, Any]:
    headers = {"Accept": "application/json"}
    body = None
    if payload is not None:
        headers["Content-Type"] = "application/json"
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = request.Request(url=url, method=method, headers=headers, data=body)
    try:
        with request.urlopen(req, timeout=timeout) as response:
            raw = response.read().decode("utf-8")
    except error.HTTPError as exc:
        details = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"HTTP {exc.code} from {url}: {details}") from exc
    parsed = json.loads(raw)
    if not isinstance(parsed, dict):
        raise RuntimeError(f"Expected a JSON object from {url}")
    if parsed.get("code") != 200:
        raise RuntimeError(f"API rejected request to {url}: {parsed}")
    return parsed


def resolve_token(
    base_url: str,
    *,
    token: str | None,
    username: str | None,
    password: str | None,
    timeout: float,
) -> str:
    resolved = token or os.getenv("COMMERCE_TOKEN")
    if resolved:
        return resolved
    if not username or not password:
        raise ValueError("Provide --token/COMMERCE_TOKEN or both --username and --password")
    response = call_json(
        f"{base_url.rstrip('/')}/api/auth/login",
        method="POST",
        payload={"username": username, "password": password},
        timeout=timeout,
    )
    try:
        return str(response["data"]["token"])
    except (KeyError, TypeError) as exc:
        raise RuntimeError(f"Login response did not contain data.token: {response}") from exc


def performance_payload(summary: dict[str, Any]) -> dict[str, Any]:
    return {
        "testName": summary["testName"],
        "testType": summary["testType"],
        "threadCount": summary["threadCount"],
        "totalRequests": summary["totalRequests"],
        "successCount": summary["successCount"],
        "failCount": summary["failCount"],
        "avgLatencyMs": round(float(summary["avgLatencyMs"]), 2),
        "maxLatencyMs": round(float(summary["maxLatencyMs"]), 2),
        "minLatencyMs": round(float(summary["minLatencyMs"]), 2),
        "throughput": round(float(summary["throughput"]), 2),
    }


def ingest_performance_result(base_url: str, token: str, summary: dict[str, Any], timeout: float) -> dict[str, Any]:
    return call_json(
        f"{base_url.rstrip('/')}/api/performance/results",
        method="POST",
        payload=performance_payload(summary),
        token=token,
        timeout=timeout,
    )


def write_ingest_response(output_path: Path, response: dict[str, Any]) -> None:
    output_path.write_text(json.dumps(response, ensure_ascii=False, indent=2), encoding="utf-8")


def describe_ingest_response(response: dict[str, Any]) -> str:
    data = response.get("data")
    if isinstance(data, dict) and data.get("resultId") is not None:
        return f"resultId={data['resultId']}"
    return json.dumps(response, ensure_ascii=False)


def load_json_object(raw: str | None, file_path: str | None, default: dict[str, Any]) -> dict[str, Any]:
    if raw and file_path:
        raise ValueError("Use either an inline JSON body or a JSON body file, not both")
    if file_path:
        value = json.loads(Path(file_path).read_text(encoding="utf-8"))
    elif raw:
        value = json.loads(raw)
    else:
        value = default
    if not isinstance(value, dict):
        raise ValueError("JSON request body must be an object")
    return value
