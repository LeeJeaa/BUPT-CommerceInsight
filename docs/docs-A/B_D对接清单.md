# 成员 A 对接清单

> 适用范围：成员 A 完成 `sql/V1` 到 `sql/V12` 后，与 B、D 进行冻结对接。  
> 核心原则：A 交付 SQL、字段语义和解释材料；B/D 按职责接入，不反向维护第二套 SQL。

---

## 1. 给 B 的交付清单

### 1.1 表结构与字段语义

交付文件：

```text
sql/V1__create_tpch_tables.sql
sql/V2__create_tpcc_tables.sql
sql/V3__create_app_tables.sql
sql/V4__add_constraints.sql
docs/database/table-contract.md
```

B 需要关注：

1. 数据库字段全部是 `snake_case`。
2. API JSON 字段全部由 B 映射成 `lowerCamelCase`。
3. C 不直接依赖数据库字段。
4. 约束异常需要在后端统一包装成 API 错误。
5. TPC-H 查询 SQL 别名保持 `snake_case` 时，必须通过 DTO/VO 或 MyBatis `ResultMap` 映射后再返回给 C。

### 1.2 TPC-H 查询接口

交付文件：

```text
sql/V5__tpch_queries.sql
docs/database/tpch-query-contract.md
```

接口映射：

| 接口 | SQL 模板 | 参数 | 重点输出 |
|---|---|---|---|
| `GET /api/tpch/q1` | `tpch_q1` | `shipDate` | `returnFlag`, `lineStatus`, `sumQuantity`, `sumCharge`, `avgPrice`, `avgDisc` |
| `GET /api/tpch/q5` | `tpch_q5` | `regionName`, `startDate`, `endDate` | `nationName`, `revenue` |
| `GET /api/tpch/q12` | `tpch_q12` | `shipMode1`, `shipMode2`, `startDate`, `endDate` | `shipMode`, `highLineCount`, `lowLineCount` |
| `GET /api/tpch/q14` | `tpch_q14` | `month` | `promoRevenuePercent` |

注意：

```text
V5 中 PREPARE 用于 A 本地验证。
B 接入 MyBatis/JDBC 时，应复制 SELECT 主体并替换为 Mapper 参数。
```

### 1.3 TPC-C 事务接口

交付文件：

```text
sql/V6__tpcc_transaction_sql.sql
docs/database/tpcc-transaction-design.md
```

B 实现要求：

1. 使用 Spring 事务。
2. 成功 commit。
3. 异常 rollback。
4. 写 `transaction_log`。
5. New-Order 扣库存前设置事务上下文：

```sql
SELECT set_config('app.transaction_id', :transactionId, true);
SELECT set_config('app.change_type', 'new_order', true);
```

6. 库存变化由 `trg_stock_quantity_change` 自动写入 `stock_change_log`。
7. 本项目 TPC-C 冻结为课程最小实现，`stock` 没有 `s_dist_01` 至 `s_dist_10`；`order_line.ol_dist_info` 由 B 传固定值（如 `dist-info-01`）或按 `districtId` 生成。

可选演示数据：

```text
V11 只加载前置数据和回滚验证，不留下已提交订单。
如 C 需要直接展示已完成订单事务样例，B/D 可在 V11 后执行 sql/demo_transaction_data.sql。
该脚本会提交一笔 New-Order、一笔 Payment，并写入 transaction_log、stock_change_log、history。
```

### 1.4 导入清洗接口

交付文件：

```text
sql/V7__import_cleaning_rules.sql
docs/database/import-cleaning-rules.md
```

B 实现要求：

1. 文件分批读取。
2. 程序侧校验类型、范围、日期、外键和主键冲突。
3. 合法行批量写入目标表。
4. 非法行写入 `import_error_log`。
5. `import_task` 更新总行数、成功行数、失败行数和状态。

---

## 2. 给 D 的交付清单

### 2.1 正式导入兼容

交付文件：

```text
sql/V1__create_tpch_tables.sql
sql/V2__create_tpcc_tables.sql
sql/V3__create_app_tables.sql
sql/V4__add_constraints.sql
docs/database/sql-execution-and-import.md
docs/database/table-contract.md
```

正式导入顺序：

```text
V1 -> V2 -> V3 -> COPY TPC-H 数据 -> V4 -> V8 -> V9 -> V10 -> 行数统计 -> EXPLAIN
```

D 需要关注：

1. `db/init/` 不维护第二套业务 SQL。
2. `00_run_sql_assets.sh` 只引用 `/sql/V*.sql`。
3. TPC-H 字段顺序以 `V1` 为准。
4. 大规模数据不提交到仓库。
5. V4 已支持重复执行，已存在约束会自动跳过；正式初始化前仍建议清库或重建 Docker volume，避免旧数据影响统计。

### 2.2 索引和 EXPLAIN

交付文件：

```text
sql/V10__indexes_baseline.sql
sql/V12__explain_baseline.sql
docs/database/index-design.md
docs/database/explain-baseline-analysis.md
```

D 执行建议：

1. 先在无 `V10` 的情况下跑一次 `V12`，保存无索引计划。
2. 执行 `V10`。
3. 再跑一次 `V12`，保存有索引计划。
4. 将结果保存到 `report/explain_plans/`。

报告解读由 A 配合完成，但正式执行和截图由 D 负责。

---

## 3. 冻结检查

冻结前 A 自查：

```text
sql/V1 到 sql/V12 是否齐全。
开发初始化顺序是否能执行。
正式导入顺序是否写清楚。
所有数据库对象是否小写 snake_case。
SQL 文件中是否没有双引号对象名。
table-contract 是否同步。
change-log 是否同步。
B/D 对接清单是否明确。
```
