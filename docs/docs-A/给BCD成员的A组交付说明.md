# 给 B/C/D 成员的 A 组交付说明

> 负责人：成员 A  
> 当前状态：数据库 SQL 基线、字段契约、事务模板、样例数据、索引/EXPLAIN 模板和 Docker 开发基线环境已完成。  
> 阅读目的：B/C/D 可以按本文档快速定位 A 已交付内容，以及开发时必须参考的文档和 SQL 文件。

---

## 1. A 目前已经完成的内容

### 1.1 SQL 源资产

`sql/` 是唯一 SQL 源资产目录，当前已完成：

| 文件 | 内容 | 给谁用 |
|---|---|---|
| `sql/V1__create_tpch_tables.sql` | TPC-H 8 张表结构 | B/D |
| `sql/V2__create_tpcc_tables.sql` | TPC-C New-Order/Payment 所需表结构 | B/D |
| `sql/V3__create_app_tables.sql` | 用户、导入、查询、事务、性能、库存日志等应用表 | B/C/D |
| `sql/V4__add_constraints.sql` | 主键、外键、唯一约束、CHECK 约束；已改为幂等，可重复执行 | B/D |
| `sql/V5__tpch_queries.sql` | TPC-H Q1/Q5/Q12/Q14 查询模板 | B/D |
| `sql/V6__tpcc_transaction_sql.sql` | TPC-C New-Order 和 Payment 事务 SQL 步骤 | B |
| `sql/V7__import_cleaning_rules.sql` | 系统演示导入的清洗和错误日志 SQL 模板 | B/C |
| `sql/V8__triggers.sql` | 库存变动日志、导入任务审计触发器 | B/C/D |
| `sql/V9__procedures.sql` | Q5/Q12 相关存储过程 | B/D |
| `sql/V10__indexes_baseline.sql` | 基线索引 | B/D |
| `sql/V11__sample_data.sql` | A/B/C 本地开发样例数据 | B/C/D |
| `sql/V12__explain_baseline.sql` | Q1/Q5/Q12/Q14 的 EXPLAIN ANALYZE 模板 | D |
| `sql/demo_transaction_data.sql` | 可选提交型 New-Order/Payment 演示数据 | B/C/D |

### 1.2 数据库契约和说明文档

已完成的核心契约包括：

```text
docs/database/table-contract.md
docs/database/tpch-query-contract.md
docs/database/tpcc-transaction-design.md
docs/database/import-cleaning-rules.md
docs/database/sql-execution-and-import.md
docs/database/index-design.md
docs/database/explain-baseline-analysis.md
docs/database/triggers-and-procedures.md
docs/api/api-contract.md
docs/api/mock-contract.md
```

### 1.3 开发数据库环境

已提供 PostgreSQL 16 开发基线环境：

```text
db/docker-compose.yml
db/init/00_run_sql_assets.sh
db/README.md
```

默认初始化顺序：

```text
V1 -> V2 -> V3 -> V4 -> V8 -> V9 -> V10 -> V11
```

可选事务演示数据：

```powershell
docker exec -it tpc-commerce-postgres psql -U tpc_admin -d tpc_commerce -f /sql/demo_transaction_data.sql
```

### 1.4 验证和报告材料

已整理 A 侧数据库验证截图和原始输出：

```text
report/screenshots/database/
report/screenshots/database/raw/
report/parts/A_数据库设计与SQL实现.md
```

---

## 2. B 后端开发需要先看哪些文档

### 2.1 必看文档

| 优先级 | 文档 | 用途 |
|---|---|---|
| 1 | `docs/docs-A/backend-adapter-pack.md` | 后端适配包，包含 B 可复制到 Mapper 的 SQL 主体和 DTO 字段映射 |
| 2 | `docs/api/api-contract.md` | API 路径、请求响应字段、统一响应格式 |
| 3 | `docs/database/table-contract.md` | 表结构、字段含义、主键外键和 CHECK 约束 |
| 4 | `docs/database/tpch-query-contract.md` | Q1/Q5/Q12/Q14 参数和返回字段 |
| 5 | `docs/database/tpcc-transaction-design.md` | New-Order/Payment 事务步骤、回滚条件、日志要求 |
| 6 | `docs/database/import-cleaning-rules.md` | 导入接口校验规则、错误原因编码、负例 SQL |

### 2.2 B 开发时特别注意

1. 数据库字段和 SQL 输出别名统一是 `snake_case`，API JSON 必须由 B 映射为 `lowerCamelCase`。
2. C 不直接读数据库字段，也不应该收到 `snake_case` 字段。
3. TPC-C 是课程最小实现，不是完整 BenchmarkSQL 兼容实现。
4. `stock` 没有 `s_dist_01` 到 `s_dist_10`，New-Order 的 `order_line.ol_dist_info` 由 B 固定传入 `dist-info-01` 或按 `districtId` 生成。
5. New-Order 扣库存前要设置事务上下文，触发器才能写入关联事务编号：

```sql
SELECT set_config('app.transaction_id', :transactionId, true);
SELECT set_config('app.change_type', 'new_order', true);
```

6. `V4__add_constraints.sql` 已支持重复执行，约束已存在时会自动跳过。
7. `import_task` 有行数一致性约束：`success_rows + failed_rows <= total_rows`。

---

## 3. C 前端开发需要先看哪些文档

### 3.1 必看文档

| 优先级 | 文档 | 用途 |
|---|---|---|
| 1 | `docs/docs-A/frontend-display-pack.md` | 前端展示包，说明页面需要展示哪些接口字段 |
| 2 | `docs/api/api-contract.md` | 真实接口字段，以此写 Axios 类型和页面字段 |
| 3 | `docs/api/mock-contract.md` | Mock 数据字段，以此先做页面和图表 |
| 4 | `docs/database/tpch-query-contract.md` | TPC-H Q1/Q5/Q12/Q14 的图表展示语义 |
| 5 | `docs/database/table-contract.md` | 只用于理解业务含义，不作为前端字段来源 |
| 6 | `docs/docs-A/integration-debug-pack.md` | 前后端联调排错清单 |

### 3.2 C 开发时特别注意

1. 前端只依赖 API/Mock 的 `lowerCamelCase` 字段。
2. 不要在前端写 `return_flag`、`nation_name`、`promo_revenue_percent` 等数据库字段名。
3. TPC-H 图表建议：
   - Q1：分组表格、汇总柱状图。
   - Q5：国家收入柱状图。
   - Q12：高/低优先级堆叠柱状图。
   - Q14：促销收入占比指标卡或图表。
4. 如果页面需要展示“已提交订单事务样例”，让 B/D 在 V11 后执行 `sql/demo_transaction_data.sql`。
5. 库存变化日志来自 `stock_change_log`，前端字段以 API 契约为准，例如 `oldQuantity`、`newQuantity`、`changeQuantity`、`relatedTransactionId`。

---

## 4. D 环境与性能开发需要先看哪些文档

### 4.1 必看文档

| 优先级 | 文档 | 用途 |
|---|---|---|
| 1 | `db/README.md` | PostgreSQL 开发基线环境启动、连接、重置 |
| 2 | `docs/database/sql-execution-and-import.md` | SQL 执行顺序、开发初始化和正式导入模式 |
| 3 | `docs/integration/database-deploy-checklist.md` | Docker 初始化和部署检查项 |
| 4 | `docs/database/table-contract.md` | COPY 字段顺序、表结构、约束说明 |
| 5 | `docs/database/index-design.md` | 索引设计和执行顺序 |
| 6 | `docs/database/explain-baseline-analysis.md` | EXPLAIN 保存方式、分析模板、样例计划说明 |
| 7 | `docs/docs-A/performance-test-pack.md` | 性能测试对接包 |
| 8 | `docs/docs-A/数据库基线环境验证记录.md` | A 侧已完成的基线验证记录 |

### 4.2 D 开发时特别注意

1. `db/init/` 只做初始化入口，不维护第二套业务 SQL。
2. Docker 开发初始化可用：

```powershell
docker compose -f db/docker-compose.yml up -d
```

3. 重置开发库建议：

```powershell
docker compose -f db/docker-compose.yml down -v
docker compose -f db/docker-compose.yml up -d
```

4. 正式导入推荐顺序：

```text
V1 -> V2 -> V3 -> COPY TPC-H 数据 -> V4 -> V8 -> V9 -> V10 -> 行数统计 -> EXPLAIN
```

5. `V4` 已幂等，可重复执行，但正式性能测试仍建议使用干净数据库，避免旧数据污染行数和性能结果。
6. `V10` 索引建议在 COPY 后执行，避免大规模导入时维护索引拖慢速度。
7. `V12` 是 EXPLAIN 模板，样例数据上的执行计划只证明 SQL 可执行，不作为正式性能结论。
8. 正式性能结论应基于 D 的 SF=0.1/SF=0.6/SF=1 数据、压测结果和 EXPLAIN ANALYZE 输出。

---

## 5. 共同约定

### 5.1 命名约定

| 层级 | 命名 |
|---|---|
| PostgreSQL 表字段 | `snake_case` |
| SQL 查询输出别名 | `snake_case` |
| Java DTO/VO | `lowerCamelCase` |
| API JSON | `lowerCamelCase` |
| Vue/Mock | `lowerCamelCase` |

### 5.2 TPC-C 口径

```text
本项目 TPC-C 冻结为课程最小实现，只覆盖 New-Order 和 Payment 演示所需字段。
stock 不包含 s_dist_01~s_dist_10。
ol_dist_info 由 B 生成，不从 stock 表读取。
```

### 5.3 样例数据口径

```text
V11 只提供基础样例数据和回滚验证，不持久化已提交订单。
如果需要已提交事务样例，执行 sql/demo_transaction_data.sql。
正式性能测试数据由 D 使用 dbgen/COPY 准备，不能用 V11 代表正式 benchmark 数据。
```

### 5.4 谁遇到问题先看哪里

| 问题 | 先看 |
|---|---|
| 字段不知道怎么映射 | `docs/database/table-contract.md`、`docs/api/api-contract.md` |
| TPC-H 查询接口对不上 | `docs/database/tpch-query-contract.md`、`docs/docs-A/backend-adapter-pack.md` |
| New-Order/Payment 不知道怎么实现 | `docs/database/tpcc-transaction-design.md` |
| 导入错误日志和校验规则不清楚 | `docs/database/import-cleaning-rules.md` |
| Docker 初始化失败 | `db/README.md`、`docs/integration/database-deploy-checklist.md` |
| EXPLAIN 怎么保存和分析 | `docs/database/explain-baseline-analysis.md` |
| 前后端联调字段错位 | `docs/docs-A/integration-debug-pack.md` |

---

## 6. 建议阅读顺序

### B 后端

```text
docs/docs-A/backend-adapter-pack.md
docs/api/api-contract.md
docs/database/table-contract.md
docs/database/tpch-query-contract.md
docs/database/tpcc-transaction-design.md
docs/database/import-cleaning-rules.md
```

### C 前端

```text
docs/docs-A/frontend-display-pack.md
docs/api/mock-contract.md
docs/api/api-contract.md
docs/database/tpch-query-contract.md
docs/docs-A/integration-debug-pack.md
```

### D 环境与性能

```text
db/README.md
docs/database/sql-execution-and-import.md
docs/integration/database-deploy-checklist.md
docs/database/index-design.md
docs/database/explain-baseline-analysis.md
docs/docs-A/performance-test-pack.md
```
