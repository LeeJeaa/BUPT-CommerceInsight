# 触发器与存储过程规范

## 1. 触发器

### trg_stock_quantity_change

用途：当 `stock` 库存数量变化时写入 `stock_change_log`。

要求：

```text
记录 warehouse_id
记录 item_id
记录 old_quantity
记录 new_quantity
记录 change_quantity
记录 change_type
记录 related_transaction_id
记录 changed_at
```

SQL 资产位置：

```text
sql/V8__triggers.sql
```

实现函数：

```text
fn_log_stock_quantity_change
```

B 在 New-Order 扣库存前可设置：

```sql
SELECT set_config('app.transaction_id', :transactionId, true);
SELECT set_config('app.change_type', 'new_order', true);
```

如果未设置，触发器默认使用 `new_order` 作为 `change_type`，`related_transaction_id` 可以为空。触发器仍兼容读取旧的 `app.change_reason`，但新代码应使用 `app.change_type`。

> ⚠️ **B 注意**：`stock_change_log.change_type` 受 `ck_stock_change_log_type_valid` 约束，只允许以下三个值：
> - `new_order`
> - `manual_adjustment`
> - `rollback`
>
> 若通过 `set_config('app.change_type', ...)` 传入不在上述枚举内的值，触发器写入 `stock_change_log` 时会因 CHECK 约束失败而导致整个事务回滚。B 使用自定义 `change_type` 前必须先在 V4 中扩展枚举值。

### trg_import_task_audit

用途：导入任务状态变化时补全时间字段或记录审计信息。

要求：

```text
任务开始时写 started_at
任务结束时写 ended_at
状态只允许 pending/running/success/failed
```

实现函数：

```text
fn_audit_import_task_status
```

行为：

```text
status = running 时，如果 started_at 为空，自动写当前时间。
status = success 或 failed 时，如果 ended_at 为空，自动写当前时间。
status = pending 时，清空 started_at 和 ended_at。
```

## 2. 存储过程

### sp_analyze_region_revenue

封装区域收入分析逻辑，可服务 Q5 或报告展示。

SQL 资产位置：

```text
sql/V9__procedures.sql
```

调用示例：

```sql
SELECT * FROM sp_analyze_region_revenue('ASIA', DATE '1994-01-01', DATE '1995-01-01');
```

输出字段：

```text
nation_name
revenue
```

### sp_analyze_shipping_priority

封装运送方式和订单优先级统计逻辑，可服务 Q12 或报告展示。

调用示例：

```sql
SELECT * FROM sp_analyze_shipping_priority('MAIL', 'SHIP', DATE '1994-01-01', DATE '1995-01-01');
```

输出字段：

```text
ship_mode
high_line_count
low_line_count
```

## 3. 验收证据

```text
触发器创建 SQL
存储过程创建 SQL
调用示例
执行结果截图
异常或回滚示例
```

推荐截图：

```text
1. SELECT * FROM stock_change_log;
2. SELECT * FROM import_task;
3. SELECT * FROM sp_analyze_region_revenue(...);
4. SELECT * FROM sp_analyze_shipping_priority(...);
```

