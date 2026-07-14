# 截图证据目录

本目录保存课程验收和报告引用的截图证据。

命名规则：

```text
YYYYMMDD_<module>_<action>_<scale-or-context>.png
```

示例：

```text
20260708_docker_verify_sf02.png
20260708_tpch_load_sf02.png
20260708_row_counts_sf02.png
20260708_explain_q1_with_index_sf02.png
20260708_backend_health_dev.png
20260708_frontend_performance_mock.png
```

要求：

- 不使用默认截图文件名。
- 每张截图必须能从文件名判断来源和用途。
- 能用文本日志证明的操作，保留 `.log/.txt` 原始文本，同时保留一张 PNG 便于报告插图。

2026-07-13 SF=0.6 正式证据：

- `20260713_dashboard_real_api_sf06.png`
- `20260713_performance_tpch_real_api_sf06.png`
- `20260713_performance_tpcc_real_api_course_minimal.png`
