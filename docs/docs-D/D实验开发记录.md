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

## 8. SF=0.6 正式数据实验

2026-07-13 使用 dbgen 2.17.2 在本机生成 SF=0.6 数据，原始 `.tbl` 约 658 MB。执行破坏性重置前，已将原 SF=0.1 数据库备份为 PostgreSQL custom-format dump，并通过 `pg_restore -l` 校验归档结构。

正式执行顺序为：`V1/V2/V3 -> COPY -> V4/V8/V9/V13 -> 无索引计划 -> V10 -> 有索引计划`。

| 表 | SF=0.6 行数 |
|---|---:|
| region | 5 |
| nation | 25 |
| supplier | 6,000 |
| customer | 90,000 |
| part | 120,000 |
| partsupp | 480,000 |
| orders | 900,000 |
| lineitem | 3,601,036 |
| 合计 | 5,197,066 |

主要证据：

- `report/import_logs/generate_tpch_sf0.6_20260713_212815.log`
- `report/import_logs/load_tpch_sf0.6_20260713_213147.log`
- `report/import_logs/row_counts_sf0.6_20260713_213316.txt`
- `report/screenshots/20260713_dashboard_real_api_sf06.png`

## 9. SF=0.6 索引前后执行计划

| 查询 | 无索引执行时间 ms | 有索引执行时间 ms | 结果说明 |
|---|---:|---:|---|
| Q1 | 1,226.754 | 1,127.723 | 有索引状态略快，但主要仍为大范围聚合扫描 |
| Q5 | 211.818 | 343.737 | 本次有索引状态较慢，说明索引并不保证所有查询都提速 |
| Q12 | 330.118 | 196.588 | 组合条件索引降低过滤成本 |
| Q14 | 188.459 | 96.691 | 日期索引将顺序扫描改为 Bitmap Index Scan |

共归档 8 份 TXT 计划和 2 份 JSON 清单，文件名均包含查询、索引状态、SF=0.6 和时间戳。数据库最后执行有索引模式，当前保持 V10 基线索引状态。

## 10. 正式并发结果与结果入库

| 操作 | 数据规模 | 线程 | 请求 | 成功/失败 | 平均延迟 ms | 最大/最小 ms | QPS/TPS |
|---|---|---:|---:|---:|---:|---:|---:|
| TPC-H Q1 | SF=0.6 | 4 | 20 | 20/0 | 1,427.046 | 2,006.415 / 1,075.836 | 2.602 QPS |
| TPC-H Q5 | SF=0.6 | 4 | 20 | 20/0 | 753.813 | 1,958.979 / 432.819 | 5.303 QPS |
| TPC-H Q12 | SF=0.6 | 4 | 20 | 20/0 | 302.463 | 343.903 / 253.591 | 13.093 QPS |
| TPC-H Q14 | SF=0.6 | 4 | 20 | 20/0 | 156.777 | 164.629 / 147.798 | 25.432 QPS |
| TPC-C New-Order | course-minimal | 4 | 20 | 20/0 | 103.586 | 224.163 / 53.255 | 36.551 TPS |
| TPC-C Payment | course-minimal | 4 | 20 | 20/0 | 52.184 | 70.678 / 31.655 | 70.790 TPS |

TPC-H 四组共 80 次请求、TPC-C 两组共 40 次请求，失败数均为 0。每组均生成 JSON、summary CSV、samples CSV 和接口写入响应 JSON，后端返回 `code=200`。

TPC-C 使用课程最小事务数据，其规模与 TPC-H SF=0.6 相互独立。新增 `run-tpcc-explain.ps1` 保存 New-Order 和 Payment 的代表性语句计划，所有修改语句在事务中执行并最终回滚；执行前后订单、历史、库存和地区序号一致。

## 11. 截图与剩余边界

新增具名截图：

- `20260713_dashboard_real_api_sf06.png`
- `20260713_performance_tpch_real_api_sf06.png`
- `20260713_performance_tpcc_real_api_course_minimal.png`

SF=0.6 正式性能证据已经完成。SF=1 尚未执行，可作为机器资源允许时的扩展实验；不得将当前结果表述为 SF=1 结论。后端逐条 SQL 计时和前端明细展示不属于本轮数据库部署与压测脚本修改范围。
