# TPC-C 事务设计规范

## 1. 通用事务要求

```text
使用 Spring 事务管理。
成功 commit。
异常 rollback。
记录 transaction_log。
New-Order 库存变化记录 stock_change_log。
返回 transactionId/status/elapsedMs。
```

实现口径冻结：

```text
本项目 TPC-C 冻结为“课程最小实现”，目标是支撑 New-Order 和 Payment 的事务完整性演示。
它不是完整 BenchmarkSQL 兼容实现。
stock 表不包含标准 TPC-C 的 s_dist_01~s_dist_10。
order_line.ol_dist_info 由 B 在 Mapper/Service 中传入固定值或按 districtId 生成。
```

## 2. New-Order

涉及表：

```text
warehouse
district
tpcc_customer
tpcc_orders
new_order
order_line
item
stock
stock_change_log
transaction_log
```

流程：

```text
1. 校验 warehouse/district/customer。
2. 获取并递增 district 下一订单号。
3. 插入 tpcc_orders。
4. 插入 new_order。
5. 对每个商品校验 item 和 stock。
6. 扣减 stock 数量。
7. 插入 order_line。
8. 写 stock_change_log。
9. 写 transaction_log。
10. commit；任一步失败 rollback。
```

SQL 资产位置：

```text
sql/V6__tpcc_transaction_sql.sql
```

成员 B 接入顺序：

| 步骤 | SQL 模板 | 作用 |
|---|---|---|
| 1 | `tpcc_no_get_context` | 校验仓库、地区、客户，并读取税率、折扣和下一订单号 |
| 2 | `tpcc_no_increment_next_order_id` | 在同一事务中递增 `district.d_next_o_id` 并返回订单号 |
| 3 | `tpcc_no_insert_order` | 写入 `tpcc_orders` |
| 4 | `tpcc_no_insert_new_order` | 写入 `new_order` |
| 5 | `tpcc_no_get_item_stock` | 校验商品和库存 |
| 6 | `tpcc_no_update_stock` | 扣减库存并更新库存统计 |
| 7 | `tpcc_no_insert_order_line` | 写入订单明细（第9个参数 `ol_dist_info` 由 B 传入，本项目统一传固定字符串或按 `districtId` 生成即可，见 V6 注释说明）|
| 8 | `tpcc_insert_transaction_log` | 写入事务日志 |

库存日志说明：

```text
B 在执行 tpcc_no_update_stock 前，可以设置事务内配置：
SELECT set_config('app.transaction_id', :transactionId, true);
SELECT set_config('app.change_type', 'new_order', true);

V8 中的 trg_stock_quantity_change 会在 stock.s_quantity 变化时写入 stock_change_log。
```

回滚条件：

```text
warehouse/district/customer 不存在
item 不存在
stock 不存在
stock.s_quantity 小于购买数量
任一 insert/update 影响行数不符合预期
任一约束异常
```

## 3. Payment

涉及表：

```text
warehouse
district
tpcc_customer
history
transaction_log
```

流程：

```text
1. 校验 warehouse/district/customer。
2. 更新 warehouse 收款金额。
3. 更新 district 收款金额。
4. 更新 customer 余额和支付次数。
5. 插入 history。
6. 写 transaction_log。
7. commit；任一步失败 rollback。
```

SQL 资产位置：

```text
sql/V6__tpcc_transaction_sql.sql
```

成员 B 接入顺序：

| 步骤 | SQL 模板 | 作用 |
|---|---|---|
| 1 | `tpcc_pay_get_context` | 校验仓库、地区、客户，并读取当前客户余额 |
| 2 | `tpcc_pay_update_warehouse` | 更新仓库累计收款 |
| 3 | `tpcc_pay_update_district` | 更新地区累计收款 |
| 4 | `tpcc_pay_update_customer` | 更新客户余额、累计支付金额和支付次数 |
| 5 | `tpcc_pay_insert_history` | 写入支付历史 |
| 6 | `tpcc_insert_transaction_log` | 写入事务日志 |

回滚条件：

```text
warehouse/district/customer 不存在
paymentAmount 小于等于 0
任一 update 影响行数不是 1
history 写入失败
任一约束异常
```

## 4. 返回字段对齐

New-Order 返回：

```text
transactionId
status
orderId
totalAmount
elapsedMs
```

Payment 返回：

```text
transactionId
status
customerId
newBalance
elapsedMs
```

数据库字段仍使用 `snake_case`，由 B 在 DTO/VO 或 MyBatis `ResultMap` 中强制映射到 API 的 `lowerCamelCase`。C 不读取 SQL 输出别名或数据库字段。

## 5. 可执行事务示例

以下示例基于 `V11__sample_data.sql` 的最小样例数据，完整版本同时保存在 `sql/V6__tpcc_transaction_sql.sql` 附录注释中。
若需要在初始化库中留下已提交的订单、订单明细、支付历史和事务日志样例，请在 V11 之后手动执行 `sql/demo_transaction_data.sql`。

### 5.1 New-Order 提交示例

```sql
\i sql/V6__tpcc_transaction_sql.sql

BEGIN;
SELECT set_config('app.transaction_id', 'no-demo-commit-3001', true);
SELECT set_config('app.change_type', 'new_order', true);

EXECUTE tpcc_no_get_context(1, 1, 1);
EXECUTE tpcc_no_increment_next_order_id(1, 1);
EXECUTE tpcc_no_insert_order(1, 1, 3001, 1, 1, 1);
EXECUTE tpcc_no_insert_new_order(1, 1, 3001);
EXECUTE tpcc_no_get_item_stock(1, 1001);
EXECUTE tpcc_no_update_stock(1, 1001, 2);
EXECUTE tpcc_no_insert_order_line(1, 1, 3001, 1, 1001, 1, 2, 50.20, 'dist-info-01');
EXECUTE tpcc_insert_transaction_log('no-demo-commit-3001', 'new_order', 'committed', 0, NULL, 1);

COMMIT;
```

### 5.2 New-Order 回滚示例

```sql
BEGIN;
SELECT set_config('app.transaction_id', 'no-demo-rollback', true);
SELECT set_config('app.change_type', 'new_order', true);

EXECUTE tpcc_no_get_context(1, 1, 1);
EXECUTE tpcc_no_get_item_stock(1, 1001);
EXECUTE tpcc_no_update_stock(1, 1001, 100000);

ROLLBACK;
```

### 5.3 Payment 提交示例

```sql
BEGIN;

EXECUTE tpcc_pay_get_context(1, 1, 1);
EXECUTE tpcc_pay_update_warehouse(1, 25.00);
EXECUTE tpcc_pay_update_district(1, 1, 25.00);
EXECUTE tpcc_pay_update_customer(1, 1, 1, 25.00);
EXECUTE tpcc_pay_insert_history(1, 1, 1, 1, 1, 25.00, 'payment demo');
EXECUTE tpcc_insert_transaction_log('pay-demo-commit', 'payment', 'committed', 0, NULL, 1);

COMMIT;
```

### 5.4 Payment 回滚示例

```sql
BEGIN;

EXECUTE tpcc_pay_get_context(1, 1, 1);
EXECUTE tpcc_pay_update_warehouse(1, -25.00);

ROLLBACK;
```
