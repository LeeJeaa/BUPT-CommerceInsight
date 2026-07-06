# 表结构契约

## 1. 冻结规则

```text
A 修改表结构前必须更新本文档。
B 只通过 DTO/Mapper 适配数据库字段。
C 不读取本文档作为前端字段来源，只读取 API/Mock 契约。
D 使用本文档检查 COPY 字段顺序和行数统计。
```

## 2. 表清单

| 类型 | 表 |
|---|---|
| TPC-H | `region`, `nation`, `supplier`, `part`, `partsupp`, `customer`, `orders`, `lineitem` |
| TPC-C | `warehouse`, `district`, `tpcc_customer`, `history`, `new_order`, `tpcc_orders`, `order_line`, `item`, `stock` |
| 应用辅助 | `app_user`, `import_task`, `import_error_log`, `query_log`, `transaction_log`, `performance_result`, `stock_change_log` |

## 3. 应用辅助表字段

| 表 | 主键 | 关键字段 |
|---|---|---|
| `app_user` | `user_id` | `username`, `password_hash`, `role`, `status`, `created_at` |
| `import_task` | `task_id` | `table_name`, `file_name`, `status`, `total_rows`, `success_rows`, `failed_rows` |
| `import_error_log` | `error_id` | `task_id`, `line_no`, `field_name`, `raw_value`, `reason` |
| `query_log` | `query_log_id` | `query_name`, `query_params`, `elapsed_ms`, `row_count`, `executed_by` |
| `transaction_log` | `transaction_log_id` | `transaction_id`, `transaction_type`, `status`, `elapsed_ms`, `error_message` |
| `performance_result` | `result_id` | `test_name`, `thread_count`, `avg_latency_ms`, `throughput_qps`, `success_requests`, `failed_requests` |
| `stock_change_log` | `log_id` | `warehouse_id`, `item_id`, `old_quantity`, `new_quantity`, `change_quantity`, `change_reason`, `related_transaction_id` |

## 4. 变更记录要求

每次表字段变更必须记录：

```text
变更日期
变更人
变更表
新增/删除/修改字段
是否影响 B Mapper/DTO
是否影响 D COPY 或统计脚本
```

