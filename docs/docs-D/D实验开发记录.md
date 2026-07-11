# D 实验开发记录

> 负责人：成员 D  
> 目的：记录 Docker、正式数据导入、行数统计、EXPLAIN、并发测试的开发过程和证据位置。本文不是最终报告正文，后续报告只引用已验证结果。

## 1. 指导书与分工要求摘要

D 侧工作必须覆盖以下要求：

- Docker PostgreSQL 运行环境，统一连接口径。
- TPC-H 正式数据 COPY 导入，保留导入命令、耗时和日志。
- 导入后执行表行数统计，保存文本结果和截图证据。
- 执行 Q1/Q5/Q12/Q14 的 EXPLAIN ANALYZE，保存无索引和有索引对比结果。
- 执行 TPC-H/TPC-C 并发测试，记录线程数、总请求数、成功数、失败数、平均延迟、最大/最小延迟、吞吐量。
- 报告整合阶段引用 A/B/C 交付材料，但不替 A/B/C 修改专业实现。

## 2. 当前本地数据口径

课程提供的本地 TPC-H 数据目录：

```text
D:\git\TPC CommerceInsight\资料\tpc-h数据(2)
```

当前数据文件总大小约 214 MB。已知行数：

| 表 | 行数 |
|---|---:|
| region | 5 |
| nation | 25 |
| supplier | 2000 |
| customer | 30000 |
| part | 40000 |
| partsupp | 160000 |
| orders | 300000 |
| lineitem | 1199969 |

说明：该数据集可用于当前闭环验证。若最终验收严格要求 600 MB 或 SF=0.6/SF=1，需要继续通过 dbgen 或课程提供的更大数据集补齐。

## 3. 证据保存规范

文本日志保存到：

```text
report/import_logs/
report/explain_plans/
report/performance/
```

截图保存到：

```text
report/screenshots/
```

截图文件名必须包含日期、模块、动作和数据规模，不使用 `image.png`、`screenshot.png`、`Snipaste_默认名` 等默认名称。

示例：

```text
20260708_docker_verify_sf02.png
20260708_tpch_load_sf02.png
20260708_row_counts_sf02.png
20260708_explain_q1_with_index_sf02.png
20260708_performance_q1_threads4_sf02.png
```

## 4. 执行记录

| 日期 | 动作 | 结果 | 文本证据 | 截图证据 |
|---|---|---|---|---|
| 2026-07-08 | 合并 ABC 最新 develop 到 D 分支 | 成功，无冲突 | Git 提交 `b135a11` | 待补 |
| 2026-07-08 | Docker 环境验证 | 通过，Docker daemon、Compose、LF、目录和 SQL 资产均可用 | `report/import_logs/verify_environment_20260708_213309.txt` | `report/screenshots/20260708_213309_docker_verify_environment.png` |
| 2026-07-08 | 无索引基线库重置 | 通过，`LOAD_BASELINE_INDEXES=false` | `report/import_logs/reset_db_20260708_213333.log` | `report/screenshots/20260708_213335_docker_reset_no_index.png` |
| 2026-07-08 | PostgreSQL 连接验证 | 通过，PostgreSQL 16.14，库名 `tpc_commerce` | `report/import_logs/smoke_db_20260708_213346.txt` | `report/screenshots/20260708_213346_docker_smoke_no_index.png` |
| 2026-07-08 | COPY 导入课程 TPC-H 数据 | 通过，8 张 TPC-H 表全部导入 | `report/import_logs/load_tpch_sf0.2_20260708_213417.log` | `report/screenshots/20260708_213449_tpch_load_sf02_no_index.png` |
| 2026-07-08 | 行数统计 | 通过，`lineitem=1199969`、`orders=300000` | `report/import_logs/row_counts_20260708_213456.txt` | `report/screenshots/20260708_213457_row_counts_sf02_no_index.png` |
| 2026-07-08 | 无索引 EXPLAIN ANALYZE | 通过，Q1/Q5/Q12/Q14 均有结果 | `report/explain_plans/tpch_explain_without_index_sf0.2_20260708_213609.txt` | `report/screenshots/20260708_213611_explain_without_index_sf02.png` |
| 2026-07-08 | 执行 V10 基线索引 | 通过，12 个索引创建完成 | `report/import_logs/apply_v10_indexes_20260708_213618.txt` | `report/screenshots/20260708_213618_apply_v10_indexes_sf02.png` |
| 2026-07-08 | 有索引 EXPLAIN ANALYZE | 通过，Q12/Q14 命中索引且耗时下降 | `report/explain_plans/tpch_explain_with_index_sf0.2_20260708_213627.txt` | `report/screenshots/20260708_213629_explain_with_index_sf02.png` |
| 2026-07-08 | TPC-H 数据库并发测试 | 通过，Q1/Q5/Q12/Q14 均为 4 线程 20 请求、失败数 0 | `report/performance/db_concurrent_summary_sf02.md` | `report/screenshots/20260708_213844_performance_db_q1_threads4_sf02.png` 等 |

## 5. 后续待执行

1. 启动 Docker Desktop，重新验证 `db/scripts/verify-environment.ps1`。
2. 重置 PostgreSQL 容器，执行正式初始化。
3. 用课程数据集执行 COPY 导入。
4. 执行行数统计并保存日志/截图。
5. 执行 ANALYZE 和 EXPLAIN ANALYZE，保存正式结果。
6. 等后端可启动后执行 HTTP 并发压测，保存 JSON/CSV/截图。
