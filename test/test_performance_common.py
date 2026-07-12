from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path

from performance_common import Sample, load_json_object, performance_payload, summarize, write_outputs


class PerformanceCommonTest(unittest.TestCase):
    def test_summary_uses_backend_contract_fields_and_qps(self) -> None:
        samples = [
            Sample("q1", True, 10.0, 200),
            Sample("q1", True, 20.0, 200),
            Sample("q1", False, 30.0, 500, "failed"),
        ]
        summary = summarize(
            test_name="TPC-H Q1",
            test_type="tpch",
            operation="q1",
            scale_factor="0.1",
            threads=2,
            target_url="http://localhost/api/tpch/q1",
            samples=samples,
            elapsed_seconds=1.5,
            started_at="start",
            ended_at="end",
        )

        self.assertEqual(summary["successCount"], 2)
        self.assertEqual(summary["failCount"], 1)
        self.assertEqual(summary["totalRequests"], 3)
        self.assertEqual(summary["qps"], 2.0)
        self.assertNotIn("successRequests", summary)
        self.assertNotIn("failedRequests", summary)

    def test_backend_payload_excludes_archive_only_fields(self) -> None:
        summary = {
            "testName": "TPC-C Payment",
            "testType": "tpcc",
            "threadCount": 4,
            "totalRequests": 20,
            "successCount": 20,
            "failCount": 0,
            "avgLatencyMs": 12.345,
            "maxLatencyMs": 30.123,
            "minLatencyMs": 5.555,
            "throughput": 80.678,
            "tps": 80.678,
            "scaleFactor": "0.1",
        }
        payload = performance_payload(summary)
        self.assertEqual(payload["avgLatencyMs"], 12.35)
        self.assertEqual(payload["throughput"], 80.68)
        self.assertNotIn("scaleFactor", payload)
        self.assertNotIn("tps", payload)

    def test_outputs_always_include_json_and_csv(self) -> None:
        summary = {
            "testName": "TPC-H Q1",
            "testType": "tpch",
            "operation": "q1",
            "scaleFactor": "0.1",
            "threadCount": 1,
            "totalRequests": 1,
            "successCount": 1,
            "failCount": 0,
            "avgLatencyMs": 1.0,
            "p95LatencyMs": 1.0,
            "maxLatencyMs": 1.0,
            "minLatencyMs": 1.0,
            "throughput": 1.0,
            "qps": 1.0,
            "startedAt": "start",
            "endedAt": "end",
            "elapsedSeconds": 1.0,
            "targetUrl": "http://localhost",
        }
        with tempfile.TemporaryDirectory() as temp_dir:
            paths = write_outputs(Path(temp_dir), "q1 test", summary, [Sample("q1", True, 1.0, 200)])
            self.assertTrue(paths["json"].exists())
            self.assertTrue(paths["summaryCsv"].exists())
            self.assertTrue(paths["samplesCsv"].exists())
            payload = json.loads(paths["json"].read_text(encoding="utf-8"))
            self.assertEqual(payload["summary"]["successCount"], 1)

    def test_json_body_can_be_loaded_from_file(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            body_path = Path(temp_dir) / "body.json"
            body_path.write_text('{"warehouseId": 1}', encoding="utf-8")
            self.assertEqual(load_json_object(None, str(body_path), {}), {"warehouseId": 1})


if __name__ == "__main__":
    unittest.main()
