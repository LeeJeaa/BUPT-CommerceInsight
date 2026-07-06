# 数据库设计规范

## 1. 命名规范

```text
表名、字段名、索引名、约束名、触发器名、函数名全部使用小写 snake_case。
禁止使用双引号创建数据库对象。
数据库字段不直接服务前端命名偏好。
```

约束命名：

| 类型 | 格式 |
|---|---|
| 主键 | `pk_<table>` |
| 外键 | `fk_<from_table>_<to_table>` |
| 唯一约束 | `uk_<table>_<columns>` |
| 检查约束 | `ck_<table>_<condition>` |
| 索引 | `idx_<table>_<columns>` |
| 触发器 | `trg_<table>_<action>` |
| 函数 | `fn_<business_name>` |
| 存储过程 | `sp_<business_name>` |

## 2. TPC-H 表

```text
region
nation
supplier
part
partsupp
customer
orders
lineitem
```

字段保持 TPC-H 原始语义和前缀，例如：

```text
orders.o_orderkey
orders.o_custkey
lineitem.l_orderkey
lineitem.l_linenumber
customer.c_custkey
```

## 3. TPC-C 必要表

```text
warehouse
district
tpcc_customer
history
new_order
tpcc_orders
order_line
item
stock
```

为避免与 TPC-H 冲突，TPC-C 客户和订单使用 `tpcc_customer`、`tpcc_orders`。

TPC-C 实现口径冻结为课程最小实现，只覆盖 New-Order 和 Payment 事务演示所需字段，不追求完整 BenchmarkSQL 兼容。`stock` 不包含标准 TPC-C 的 `s_dist_01` 至 `s_dist_10`，`order_line.ol_dist_info` 由 B 在 Service/Mapper 中传入固定值或按 `districtId` 生成。

## 4. 应用辅助表

### app_user

```text
user_id
username
password_hash
real_name
email
role
status
created_at
updated_at
```

### import_task

```text
task_id
table_name
file_name
status
total_rows
success_rows
failed_rows
started_at
ended_at
created_by
```

### import_error_log

```text
error_id
task_id
line_number
field_name
field_value
error_reason
created_at
```

### query_log

```text
query_log_id
query_name
query_params
elapsed_ms
row_count
executed_by
executed_at
```

### transaction_log

```text
transaction_log_id
transaction_id
transaction_type
status
elapsed_ms
error_message
executed_by
executed_at
```

### performance_result

```text
result_id
test_name
test_type
thread_count
total_requests
success_count
fail_count
avg_latency_ms
max_latency_ms
min_latency_ms
throughput
created_at
```

### stock_change_log

```text
log_id
warehouse_id
item_id
old_quantity
new_quantity
change_quantity
change_type
related_transaction_id
changed_at
```

用途：记录 TPC-C New-Order 等事务造成的库存变化，支撑触发器演示、事务审计和报告截图。

## 5. 核心查询

至少实现：

```text
Q1 定价汇总报表查询
Q5 本地供应商收入量查询
Q12 运送方式和订单优先级查询
Q14 促销效果查询
```

每个查询必须记录：

```text
查询名称
输入参数
输出字段
SQL 文件位置
适合图表类型
推荐索引
EXPLAIN 观察点
```

## 6. TPC-C 事务

New-Order 必须包含：

```text
检查客户、仓库、地区
创建订单
插入 new_order
扣减 stock
写 order_line
写 stock_change_log
提交或回滚
```

Payment 必须包含：

```text
更新 warehouse/district 收款金额
更新 customer 余额
写 history
写 transaction_log
提交或回滚
```

## 7. 索引初版

```sql
CREATE INDEX idx_orders_orderdate ON orders(o_orderdate);
CREATE INDEX idx_orders_custkey ON orders(o_custkey);
CREATE INDEX idx_lineitem_orderkey ON lineitem(l_orderkey);
CREATE INDEX idx_lineitem_shipdate ON lineitem(l_shipdate);
CREATE INDEX idx_lineitem_shipmode_receiptdate ON lineitem(l_shipmode, l_receiptdate);
CREATE INDEX idx_customer_nationkey ON customer(c_nationkey);
CREATE INDEX idx_supplier_nationkey ON supplier(s_nationkey);
CREATE INDEX idx_partsupp_suppkey ON partsupp(ps_suppkey);
CREATE INDEX idx_partsupp_partkey ON partsupp(ps_partkey);
```

