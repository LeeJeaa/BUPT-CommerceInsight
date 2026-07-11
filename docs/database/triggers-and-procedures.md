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
记录 change_reason
记录 related_transaction_id
记录 created_at
```

### trg_import_task_audit

用途：导入任务状态变化时补全时间字段或记录审计信息。

要求：

```text
任务开始时写 started_at
任务结束时写 ended_at
状态只允许 pending/running/success/failed
```

## 2. 存储过程

### sp_analyze_region_revenue

封装区域收入分析逻辑，可服务 Q5 或报告展示。

### sp_analyze_shipping_priority

封装运送方式和订单优先级统计逻辑，可服务 Q12 或报告展示。

## 3. 验收证据

```text
触发器创建 SQL
存储过程创建 SQL
调用示例
执行结果截图
异常或回滚示例
```

