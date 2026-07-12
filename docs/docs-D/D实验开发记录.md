# 数据库工程与性能测试开发记录

## 1. 本轮目标

本轮完成 Docker PostgreSQL、数据库升级、正式 TPC-H 数据导入、索引前后执行计划、TPC-H/TPC-C HTTP 并发测试及结果入库。SQL 仍以 `sql/` 为唯一源资产，`db/init/` 只负责调用版本化 SQL。

## 2. 数据库初始化与升级

- 新 volume：`db/init/00_run_sql_assets.sh` 已在初始化末尾执行 `V13__migrate_legacy_stock_change_log.sql`。
- 旧 volume：新增 `db/scripts/migrate-db.ps1`，不删除 volume，依次执行 V13、重建 V8 触发器并校验字段和触发器。
- 破坏性重置：`db/scripts/reset-db.ps1` 必须显式传入 `-Force`；未传入时打印风险并以退出码 2 结束。
- 2026-07-12 对已有 volume 的非破坏性迁移已成功，证据为 `report/import_logs/migrate_db_20260712_234319.log`。

## 3. SF=0.1 正式数据链路

使用 dbgen 2.17.2 生成 8 个 `.tbl` 文件，再由 `load-tpch.ps1` 处理行末分隔符并按外键顺序执行 COPY。

| 表 | 行数 |
|---|---:|
| region | 5 |
| nation | 25 |
| supplier | 1,000 |
| customer | 15,000 |
| part | 20,000 |
| partsupp | 80,000 |
| orders | 150,000 |
| lineitem | 600,572 |

证据：

- `report/import_logs/reset_db_20260713_002349.log`
- `report/import_logs/load_tpch_sf0.1_20260713_002403.log`
- `report/import_logs/row_counts_20260713_002437.txt`

## 4. EXPLAIN ANALYZE

`db/scripts/run-tpch-explain.ps1` 可按 `q1/q5/q12/q14/all` 运行，并可选择 `without_index` 或 `with_index`。无索引模式只删除 V10 定义的 TPC-H 二级索引，不删除主键和约束索引；有索引模式重新执行标准 V10 资产。

本轮已保存 8 份执行计划和 2 份清单：

- `report/explain_plans/tpch_q1_explain_without_index_sf0.1_20260713_002428.txt`
- `report/explain_plans/tpch_q5_explain_without_index_sf0.1_20260713_002428.txt`
- `report/explain_plans/tpch_q12_explain_without_index_sf0.1_20260713_002428.txt`
- `report/explain_plans/tpch_q14_explain_without_index_sf0.1_20260713_002428.txt`
- `report/explain_plans/tpch_q1_explain_with_index_sf0.1_20260713_002431.txt`
- `report/explain_plans/tpch_q5_explain_with_index_sf0.1_20260713_002431.txt`
- `report/explain_plans/tpch_q12_explain_with_index_sf0.1_20260713_002431.txt`
- `report/explain_plans/tpch_q14_explain_with_index_sf0.1_20260713_002431.txt`

有索引模式已最后执行，因此数据库当前处于标准索引状态。

## 5. HTTP 并发测试

新增独立入口：

- `test/tpch_concurrent_test.py`：Q1/Q5/Q12/Q14。
- `test/tpcc_concurrent_test.py`：New-Order/Payment，支持内联 JSON 或 JSON 文件。
- `test/performance_common.py`：并发请求、指标、CSV/JSON、登录和结果入库公共逻辑。

结果字段与后端契约一致，使用 `successCount`、`failCount`，并分别设置 `testType=tpch`、`testType=tpcc`。

| 操作 | 线程 | 请求 | 成功/失败 | 平均延迟 ms | 最大/最小 ms | QPS/TPS |
|---|---:|---:|---:|---:|---:|---:|
| TPC-H Q1 | 4 | 20 | 20/0 | 184.648 | 263.664 / 145.190 | 19.805 QPS |
| TPC-H Q5 | 4 | 20 | 20/0 | 37.891 | 61.621 / 23.178 | 95.976 QPS |
| TPC-H Q12 | 4 | 20 | 20/0 | 29.319 | 45.939 / 22.771 | 127.483 QPS |
| TPC-H Q14 | 4 | 20 | 20/0 | 19.017 | 39.708 / 14.053 | 191.746 QPS |
| TPC-C New-Order | 4 | 20 | 20/0 | 52.562 | 112.713 / 20.622 | 71.755 TPS |
| TPC-C Payment | 4 | 20 | 20/0 | 34.357 | 47.798 / 22.232 | 107.451 TPS |

每项均输出 summary JSON、summary CSV、samples CSV 和接口写入响应 JSON，位于 `report/performance/`。后端性能结果接口已实际写入成功。

## 6. 联调与验证

- Docker 环境检查：全部通过。
- 后端单元/接口测试：40 项通过，4 项真实库测试在普通测试中按设计跳过。
- 真实 PostgreSQL 集成测试：4 项通过，0 跳过。
- HTTP 全量冒烟：19 项通过，0 失败。
- 前端生产构建：通过。
- 真实 API 浏览器联调：登录、Dashboard、TPC-H Q5、性能结果页通过。
- Python 公共逻辑测试：4 项通过。

## 7. 截图命名

截图统一保存在 `report/screenshots/`，不使用默认文件名：

- `20260713_login_real_api_sf01.png`
- `20260713_dashboard_real_api_sf01.png`
- `20260713_tpch_q1_q5_q12_q14_real_api_sf01.png`
- `20260713_performance_tpch_real_api_sf01.png`

## 8. 尚未执行

SF=0.6/SF=1 取决于机器磁盘、内存和可接受测试时长。本轮已按要求先完成 SF=0.1 全链路；更大规模不影响当前脚本与接口验收，但不能把 SF=0.1 结果表述成 SF=0.6/SF=1 的性能结论。
