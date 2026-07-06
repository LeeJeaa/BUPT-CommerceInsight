# 数据库命名规范

## 1. 总原则

```text
全部使用小写 snake_case。
禁止使用双引号创建对象。
禁止混用大写字段名。
禁止为了前端显示名修改数据库字段。
```

## 2. 表名

TPC-H 表：

```text
region, nation, supplier, part, partsupp, customer, orders, lineitem
```

TPC-C 表：

```text
warehouse, district, tpcc_customer, history, new_order, tpcc_orders, order_line, item, stock
```

应用表：

```text
app_user, import_task, import_error_log, query_log, transaction_log, performance_result, stock_change_log
```

## 3. 字段名

TPC-H 字段保留原语义前缀：

```text
o_orderkey, o_custkey, l_orderkey, l_linenumber, c_custkey
```

API 字段由 B 映射为 `lowerCamelCase`：

```text
o_orderkey -> orderKey
l_linenumber -> lineNumber
elapsed_ms -> elapsedMs
```

## 4. 对象命名

| 对象 | 格式 | 示例 |
|---|---|---|
| 主键 | `pk_<table>` | `pk_orders` |
| 外键 | `fk_<from_table>_<to_table>` | `fk_lineitem_orders` |
| 唯一约束 | `uk_<table>_<columns>` | `uk_app_user_username` |
| 检查约束 | `ck_<table>_<condition>` | `ck_orders_totalprice_non_negative` |
| 索引 | `idx_<table>_<columns>` | `idx_orders_orderdate` |
| 触发器 | `trg_<table>_<action>` | `trg_stock_quantity_change` |
| 函数 | `fn_<business_name>` | `fn_update_stock_log` |
| 存储过程 | `sp_<business_name>` | `sp_analyze_region_revenue` |

注意：PostgreSQL 中通过 `CREATE FUNCTION ... RETURNS TABLE` 实现可返回结果集的存储过程逻辑（`CREATE PROCEDURE` 无法返回结果集），本项目统一使用 `CREATE FUNCTION` 实现，命名前缀仍保持 `sp_`。B 调用时使用 `SELECT * FROM sp_xxx(...)` 语法。

