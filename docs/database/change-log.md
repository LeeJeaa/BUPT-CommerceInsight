# 数据库变更记录

| 日期 | 变更人 | 脚本 | 对象 | 变更内容 | 影响 B | 影响 C | 影响 D |
|---|---|---|---|---|---|---|---|
| 2026-07-06 | Codex 修复 | `V4__add_constraints.sql` | 全部约束 | 将 V4 改为幂等脚本，重复执行时检查 `pg_constraint` 并跳过已存在约束 | 是，可重复执行 V4 修复遗漏约束 | 否 | 是，排错时不再因重复执行 V4 误判失败 |
| 2026-07-06 | Codex 修复 | `V2__create_tpcc_tables.sql`, `V6__tpcc_transaction_sql.sql`, `demo_transaction_data.sql`, `V4__add_constraints.sql` | TPC-C 课程最小实现、导入任务约束 | 明确冻结 TPC-C 为课程最小实现；新增可选提交型 New-Order/Payment 演示数据脚本；新增 `success_rows + failed_rows <= total_rows` CHECK | 是，需生成 `ol_dist_info` 并处理新增 CHECK 异常 | 是，可选演示脚本能提供已提交事务样例 | 是，重复初始化需清库，演示数据脚本不纳入默认正式导入 |
| 2026-07-06 | 文档冻结 | `V3__create_app_tables.sql` | `stock_change_log` | 应用辅助表补充库存变动日志 | 是，需要 DTO/Mapper | 是，只通过 API 字段展示 | 是，正式脚本需包含 |
| 2026-07-06 | 成员 A | `V1__create_tpch_tables.sql` | TPC-H 8 表 | 新增 TPC-H 表结构骨架，字段保持原始语义和小写前缀 | 是，Mapper 需按字段读取 | 否，C 只依赖 API | 是，D 需按字段顺序检查 COPY |
| 2026-07-06 | 成员 A | `V2__create_tpcc_tables.sql` | TPC-C 必要表 | 新增 New-Order 和 Payment 所需 TPC-C 表 | 是，事务 Mapper 需使用 | 否，C 只依赖 API | 是，正式初始化需包含 |
| 2026-07-06 | 成员 A | `V3__create_app_tables.sql` | 应用辅助表 | 新增用户、导入、查询、事务、性能和库存日志表 | 是，需要 DTO/Mapper | 是，通过 API 展示相关数据 | 是，正式初始化需包含 |
| 2026-07-06 | 成员 A | `V4__add_constraints.sql` | 全部数据库表 | 新增主键、外键、唯一约束和 CHECK 约束 | 是，需处理约束异常 | 否，C 只依赖 API 错误信息 | 是，正式导入模式中应在 COPY 后执行 |
| 2026-07-06 | 成员 A | `V11__sample_data.sql` | 样例数据 | 新增本地开发样例数据，支撑 A/B/C 联调，不作为正式 Benchmark 数据 | 是，可用于本地 Mapper 联调 | 否，C 仍用 Mock/API | 否，D 正式数据使用 dbgen/COPY |
| 2026-07-06 | 成员 A | `V5__tpch_queries.sql` | Q1/Q5/Q12/Q14 | 新增 TPC-H 查询模板和 SQL 输出别名 | 是，需映射为 API 字段 | 否，C 只依赖 API 字段 | 是，D 可用于正式 EXPLAIN |
| 2026-07-06 | 成员 A | `V6__tpcc_transaction_sql.sql` | New-Order/Payment | 新增 TPC-C 事务 SQL 模板和步骤说明 | 是，B 按步骤实现 Spring 事务 | 否，C 只依赖 API 返回 | 是，D 可用于事务压测理解 |
| 2026-07-06 | 成员 A | `V7__import_cleaning_rules.sql` | 导入清洗 | 新增系统演示导入清洗规则和错误日志模板 | 是，B 负责接口侧校验和批量写入 | 是，C 展示错误原因 | 否，正式 COPY 不使用该流程 |
| 2026-07-06 | 成员 A | `V8__triggers.sql` | 触发器 | 新增库存变动日志触发器和导入任务审计触发器 | 是，B 扣库存前可设置事务上下文 | 是，通过 API 展示日志 | 是，正式初始化需包含 |
| 2026-07-06 | 成员 A | `V9__procedures.sql` | 存储过程 | 新增 Q5/Q12 可复用数据库侧分析例程 | 是，B 可选择调用 | 否，C 只依赖 API | 是，可作为报告证据 |
| 2026-07-06 | 成员 A | `V10__indexes_baseline.sql` | 基线索引 | 新增 TPC-H、业务查询和事务日志基线索引 | 是，查询计划可能变化 | 否 | 是，D 做索引前后对比 |
| 2026-07-06 | 成员 A | `V12__explain_baseline.sql` | EXPLAIN 模板 | 新增 Q1/Q5/Q12/Q14 EXPLAIN ANALYZE 模板 | 否 | 否 | 是，D 保存正式执行计划 |
| 2026-07-06 | 成员 A | `db/docker-compose.yml`, `db/init/00_run_sql_assets.sh` | 数据库基线环境 | 新增 PostgreSQL Docker 基线环境，初始化入口只引用 `sql/V*.sql` | 是，B 可连接开发库 | 否，C 只经 API 访问 | 是，D 后续可基于此扩展正式环境 |
| 2026-07-06 | 成员 A | `V3__create_app_tables.sql`, `V5__tpch_queries.sql`, `V6__tpcc_transaction_sql.sql`, `V7__import_cleaning_rules.sql`, `V8__triggers.sql`, `V10__indexes_baseline.sql`, `V11__sample_data.sql`, `V12__explain_baseline.sql` | 查询与字段契约 | 补齐 Q1 `sum_charge/avg_price/avg_disc`，Q14 改为 `promo_revenue_percent`，统一导入错误、性能结果和库存审计字段名，补充事务样例和回滚验证块 | 是，Mapper/DTO 需使用新字段名 | 是，表格、指标卡和错误明细按新字段展示 | 是，正式初始化和 EXPLAIN 使用更新后的 SQL |

新增变更时，必须先补充本表，再提交 SQL 或代码。

