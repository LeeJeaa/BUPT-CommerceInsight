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

## 3. TPC-H 数据字典

| 表 | 字段 | 类型 | 主键 | 外键/约束说明 |
|---|---|---|---|---|
| `region` | `r_regionkey` | `integer` | 是 | 区域编号 |
| `region` | `r_name` | `char(25)` | 否 | 区域名称 |
| `region` | `r_comment` | `varchar(152)` | 否 | 备注 |
| `nation` | `n_nationkey` | `integer` | 是 | 国家编号 |
| `nation` | `n_name` | `char(25)` | 否 | 国家名称 |
| `nation` | `n_regionkey` | `integer` | 否 | FK -> `region.r_regionkey` |
| `nation` | `n_comment` | `varchar(152)` | 否 | 备注 |
| `supplier` | `s_suppkey` | `integer` | 是 | 供应商编号 |
| `supplier` | `s_name` | `char(25)` | 否 | 供应商名称 |
| `supplier` | `s_address` | `varchar(40)` | 否 | 地址 |
| `supplier` | `s_nationkey` | `integer` | 否 | FK -> `nation.n_nationkey` |
| `supplier` | `s_phone` | `char(15)` | 否 | 电话 |
| `supplier` | `s_acctbal` | `numeric(15,2)` | 否 | 账户余额 |
| `supplier` | `s_comment` | `varchar(101)` | 否 | 备注 |
| `part` | `p_partkey` | `integer` | 是 | 零部件编号 |
| `part` | `p_name` | `varchar(55)` | 否 | 名称 |
| `part` | `p_mfgr` | `char(25)` | 否 | 制造商 |
| `part` | `p_brand` | `char(10)` | 否 | 品牌 |
| `part` | `p_type` | `varchar(25)` | 否 | Q14 用于识别 `PROMO%` |
| `part` | `p_size` | `integer` | 否 | CHECK `p_size > 0` |
| `part` | `p_container` | `char(10)` | 否 | 包装 |
| `part` | `p_retailprice` | `numeric(15,2)` | 否 | CHECK `p_retailprice >= 0` |
| `part` | `p_comment` | `varchar(23)` | 否 | 备注 |
| `partsupp` | `ps_partkey` | `integer` | 是 | PK( `ps_partkey`, `ps_suppkey` ), FK -> `part.p_partkey` |
| `partsupp` | `ps_suppkey` | `integer` | 是 | PK( `ps_partkey`, `ps_suppkey` ), FK -> `supplier.s_suppkey` |
| `partsupp` | `ps_availqty` | `integer` | 否 | CHECK `ps_availqty >= 0` |
| `partsupp` | `ps_supplycost` | `numeric(15,2)` | 否 | CHECK `ps_supplycost >= 0` |
| `partsupp` | `ps_comment` | `varchar(199)` | 否 | 备注 |
| `customer` | `c_custkey` | `integer` | 是 | 客户编号 |
| `customer` | `c_name` | `varchar(25)` | 否 | 客户名称 |
| `customer` | `c_address` | `varchar(40)` | 否 | 地址 |
| `customer` | `c_nationkey` | `integer` | 否 | FK -> `nation.n_nationkey` |
| `customer` | `c_phone` | `char(15)` | 否 | 电话 |
| `customer` | `c_acctbal` | `numeric(15,2)` | 否 | 账户余额 |
| `customer` | `c_mktsegment` | `char(10)` | 否 | 市场分段 |
| `customer` | `c_comment` | `varchar(117)` | 否 | 备注 |
| `orders` | `o_orderkey` | `integer` | 是 | 订单编号 |
| `orders` | `o_custkey` | `integer` | 否 | FK -> `customer.c_custkey` |
| `orders` | `o_orderstatus` | `char(1)` | 否 | CHECK in `O/F/P` |
| `orders` | `o_totalprice` | `numeric(15,2)` | 否 | CHECK `o_totalprice >= 0` |
| `orders` | `o_orderdate` | `date` | 否 | Q5 日期过滤 |
| `orders` | `o_orderpriority` | `char(15)` | 否 | Q12 优先级判断 |
| `orders` | `o_clerk` | `char(15)` | 否 | 经办人 |
| `orders` | `o_shippriority` | `integer` | 否 | 发运优先级 |
| `orders` | `o_comment` | `varchar(79)` | 否 | 备注 |
| `lineitem` | `l_orderkey` | `integer` | 是 | PK( `l_orderkey`, `l_linenumber` ), FK -> `orders.o_orderkey` |
| `lineitem` | `l_partkey` | `integer` | 否 | FK with `l_suppkey` -> `partsupp` |
| `lineitem` | `l_suppkey` | `integer` | 否 | FK with `l_partkey` -> `partsupp` |
| `lineitem` | `l_linenumber` | `integer` | 是 | 明细行号 |
| `lineitem` | `l_quantity` | `numeric(15,2)` | 否 | CHECK `l_quantity > 0` |
| `lineitem` | `l_extendedprice` | `numeric(15,2)` | 否 | CHECK `l_extendedprice >= 0` |
| `lineitem` | `l_discount` | `numeric(15,2)` | 否 | CHECK `0 <= l_discount <= 1` |
| `lineitem` | `l_tax` | `numeric(15,2)` | 否 | CHECK `0 <= l_tax <= 1` |
| `lineitem` | `l_returnflag` | `char(1)` | 否 | Q1 分组字段 |
| `lineitem` | `l_linestatus` | `char(1)` | 否 | Q1 分组字段 |
| `lineitem` | `l_shipdate` | `date` | 否 | Q1/Q14 日期过滤 |
| `lineitem` | `l_commitdate` | `date` | 否 | Q12 日期条件 |
| `lineitem` | `l_receiptdate` | `date` | 否 | CHECK `l_receiptdate >= l_shipdate` |
| `lineitem` | `l_shipinstruct` | `char(25)` | 否 | 发运说明 |
| `lineitem` | `l_shipmode` | `char(10)` | 否 | Q12 发运方式 |
| `lineitem` | `l_comment` | `varchar(44)` | 否 | 备注 |

## 4. TPC-C 数据字典

| 表 | 字段 | 类型 | 主键 | 外键/约束说明 |
|---|---|---|---|---|
| `warehouse` | `w_id` | `integer` | 是 | 仓库编号 |
| `warehouse` | `w_name` | `varchar(16)` | 否 | 仓库名称 |
| `warehouse` | `w_street_1` | `varchar(32)` | 否 | 地址 |
| `warehouse` | `w_street_2` | `varchar(32)` | 否 | 地址 |
| `warehouse` | `w_city` | `varchar(32)` | 否 | 城市 |
| `warehouse` | `w_state` | `char(2)` | 否 | 州/省 |
| `warehouse` | `w_zip` | `char(9)` | 否 | 邮编 |
| `warehouse` | `w_tax` | `numeric(4,4)` | 否 | CHECK `0 <= w_tax <= 1` |
| `warehouse` | `w_ytd` | `numeric(12,2)` | 否 | Payment 累计收款，CHECK `w_ytd >= 0` |
| `district` | `d_id` | `integer` | 是 | PK( `d_w_id`, `d_id` ) |
| `district` | `d_w_id` | `integer` | 是 | FK -> `warehouse.w_id` |
| `district` | `d_name` | `varchar(16)` | 否 | 地区名称 |
| `district` | `d_street_1` | `varchar(32)` | 否 | 地址 |
| `district` | `d_street_2` | `varchar(32)` | 否 | 地址 |
| `district` | `d_city` | `varchar(32)` | 否 | 城市 |
| `district` | `d_state` | `char(2)` | 否 | 州/省 |
| `district` | `d_zip` | `char(9)` | 否 | 邮编 |
| `district` | `d_tax` | `numeric(4,4)` | 否 | CHECK `0 <= d_tax <= 1` |
| `district` | `d_ytd` | `numeric(12,2)` | 否 | Payment 累计收款 |
| `district` | `d_next_o_id` | `integer` | 否 | New-Order 下一订单号，CHECK `> 0` |
注意：本项目 `tpcc_customer` 按课程设计最小化实现，仅保留 New-Order 和 Payment 两个事务所需字段。标准 TPC-C 规范中的地址字段（`c_street_1`, `c_street_2`, `c_city`, `c_state`, `c_zip`, `c_phone`）已有意省略，非遗漏。如需完整 BenchmarkSQL 兼容，须在此处补充上述字段并同步 V2/V4 和 B 的 Mapper。

| `tpcc_customer` | `c_id` | `integer` | 是 | PK( `c_w_id`, `c_d_id`, `c_id` ) |
| `tpcc_customer` | `c_d_id` | `integer` | 是 | FK with `c_w_id` -> `district` |
| `tpcc_customer` | `c_w_id` | `integer` | 是 | FK with `c_d_id` -> `district` |
| `tpcc_customer` | `c_first` | `varchar(16)` | 否 | 名 |
| `tpcc_customer` | `c_middle` | `char(2)` | 否 | 默认 `OE` |
| `tpcc_customer` | `c_last` | `varchar(16)` | 否 | 姓 |
| `tpcc_customer` | `c_credit` | `char(2)` | 否 | CHECK in `GC/BC` |
| `tpcc_customer` | `c_discount` | `numeric(4,4)` | 否 | CHECK `0 <= c_discount <= 1` |
| `tpcc_customer` | `c_balance` | `numeric(12,2)` | 否 | Payment 更新余额 |
| `tpcc_customer` | `c_ytd_payment` | `numeric(12,2)` | 否 | 累计支付 |
| `tpcc_customer` | `c_payment_cnt` | `integer` | 否 | CHECK `>= 0` |
| `tpcc_customer` | `c_delivery_cnt` | `integer` | 否 | 配送次数 |
| `tpcc_customer` | `c_data` | `text` | 否 | 备注 |
| `history` | `h_id` | `bigserial` | 是 | 支付历史编号 |
| `history` | `h_c_id` | `integer` | 否 | FK with `h_c_w_id`, `h_c_d_id` -> `tpcc_customer` |
| `history` | `h_c_d_id` | `integer` | 否 | 客户地区 |
| `history` | `h_c_w_id` | `integer` | 否 | 客户仓库 |
| `history` | `h_d_id` | `integer` | 否 | FK with `h_w_id` -> `district` |
| `history` | `h_w_id` | `integer` | 否 | 仓库编号 |
| `history` | `h_date` | `timestamp` | 否 | 支付时间 |
| `history` | `h_amount` | `numeric(12,2)` | 否 | CHECK `h_amount > 0` |
| `history` | `h_data` | `varchar(24)` | 否 | 支付说明 |
| `item` | `i_id` | `integer` | 是 | 商品编号 |
| `item` | `i_im_id` | `integer` | 否 | 图片/制造商编号 |
| `item` | `i_name` | `varchar(24)` | 否 | 商品名称 |
| `item` | `i_price` | `numeric(5,2)` | 否 | CHECK `i_price >= 0` |
| `item` | `i_data` | `varchar(50)` | 否 | 备注 |
| `stock` | `s_i_id` | `integer` | 是 | PK( `s_w_id`, `s_i_id` ), FK -> `item.i_id` |
| `stock` | `s_w_id` | `integer` | 是 | FK -> `warehouse.w_id` |
| `stock` | `s_quantity` | `integer` | 否 | New-Order 扣减，CHECK `>= 0` |
| `stock` | `s_ytd` | `integer` | 否 | 年度数量，CHECK `>= 0` |
| `stock` | `s_order_cnt` | `integer` | 否 | 订单次数，CHECK `>= 0` |
| `stock` | `s_remote_cnt` | `integer` | 否 | 远程次数，CHECK `>= 0` |
| `stock` | `s_data` | `varchar(50)` | 否 | 备注 |

注意：本项目 TPC-C 冻结为课程最小实现，`stock` 不包含标准 TPC-C 的 `s_dist_01` 至 `s_dist_10`。`order_line.ol_dist_info` 不从 `stock` 读取，由 B 在 New-Order Mapper/Service 中传入固定字符串（如 `dist-info-01`）或按 `districtId` 生成。若后续改为完整 BenchmarkSQL 兼容实现，须同步补齐 V2/V4、本文档和 B 的 Mapper。

| `tpcc_orders` | `o_id` | `integer` | 是 | PK( `o_w_id`, `o_d_id`, `o_id` ) |
| `tpcc_orders` | `o_d_id` | `integer` | 是 | FK with `o_w_id` -> `district` |
| `tpcc_orders` | `o_w_id` | `integer` | 是 | 仓库编号 |
| `tpcc_orders` | `o_c_id` | `integer` | 否 | FK -> `tpcc_customer` |
| `tpcc_orders` | `o_entry_d` | `timestamp` | 否 | 下单时间 |
| `tpcc_orders` | `o_carrier_id` | `integer` | 否 | 承运人 |
| `tpcc_orders` | `o_ol_cnt` | `integer` | 否 | CHECK `o_ol_cnt > 0` |
| `tpcc_orders` | `o_all_local` | `integer` | 否 | CHECK in `0/1` |
| `new_order` | `no_o_id` | `integer` | 是 | PK( `no_w_id`, `no_d_id`, `no_o_id` ), FK -> `tpcc_orders` |
| `new_order` | `no_d_id` | `integer` | 是 | 地区编号 |
| `new_order` | `no_w_id` | `integer` | 是 | 仓库编号 |
| `order_line` | `ol_o_id` | `integer` | 是 | PK( `ol_w_id`, `ol_d_id`, `ol_o_id`, `ol_number` ), FK -> `tpcc_orders` |
| `order_line` | `ol_d_id` | `integer` | 是 | 地区编号 |
| `order_line` | `ol_w_id` | `integer` | 是 | 仓库编号 |
| `order_line` | `ol_number` | `integer` | 是 | 明细序号 |
| `order_line` | `ol_i_id` | `integer` | 否 | FK with `ol_supply_w_id` -> `stock` |
| `order_line` | `ol_supply_w_id` | `integer` | 否 | 供货仓库 |
| `order_line` | `ol_delivery_d` | `timestamp` | 否 | 配送时间 |
| `order_line` | `ol_quantity` | `numeric(6,2)` | 否 | CHECK `ol_quantity > 0` |
| `order_line` | `ol_amount` | `numeric(12,2)` | 否 | CHECK `ol_amount >= 0` |
| `order_line` | `ol_dist_info` | `char(24)` | 否 | 配送信息 |

## 5. 应用辅助表数据字典

| 表 | 字段 | 类型 | 主键 | 外键/约束说明 |
|---|---|---|---|---|
| `app_user` | `user_id` | `bigserial` | 是 | 用户编号 |
| `app_user` | `username` | `varchar(50)` | 否 | UNIQUE |
| `app_user` | `password_hash` | `varchar(255)` | 否 | 密码摘要 |
| `app_user` | `real_name` | `varchar(100)` | 否 | 真实姓名 |
| `app_user` | `email` | `varchar(120)` | 否 | 邮箱 |
| `app_user` | `role` | `varchar(20)` | 否 | CHECK in `admin/user` |
| `app_user` | `status` | `varchar(20)` | 否 | CHECK in `pending/approved/disabled` |
| `app_user` | `created_at` | `timestamp` | 否 | 创建时间 |
| `app_user` | `updated_at` | `timestamp` | 否 | 更新时间 |
| `import_task` | `task_id` | `bigserial` | 是 | 导入任务编号 |
| `import_task` | `table_name` | `varchar(50)` | 否 | 目标表 |
| `import_task` | `file_name` | `varchar(255)` | 否 | 文件名 |
| `import_task` | `status` | `varchar(20)` | 否 | CHECK in `pending/running/success/failed` |
| `import_task` | `total_rows` | `bigint` | 否 | CHECK `>= 0` |
| `import_task` | `success_rows` | `bigint` | 否 | CHECK `>= 0`，且 `success_rows + failed_rows <= total_rows` |
| `import_task` | `failed_rows` | `bigint` | 否 | CHECK `>= 0`，且 `success_rows + failed_rows <= total_rows` |
| `import_task` | `started_at` | `timestamp` | 否 | 触发器自动补全 |
| `import_task` | `ended_at` | `timestamp` | 否 | 触发器自动补全 |
| `import_task` | `created_by` | `bigint` | 否 | FK -> `app_user.user_id` |
| `import_error_log` | `error_id` | `bigserial` | 是 | 错误日志编号 |
| `import_error_log` | `task_id` | `bigint` | 否 | FK -> `import_task.task_id` |
| `import_error_log` | `line_number` | `bigint` | 否 | CHECK `line_number > 0` |
| `import_error_log` | `field_name` | `varchar(100)` | 否 | 错误字段 |
| `import_error_log` | `field_value` | `text` | 否 | 原始字段值 |
| `import_error_log` | `error_reason` | `text` | 否 | 错误原因 |
| `import_error_log` | `created_at` | `timestamp` | 否 | 创建时间 |
| `query_log` | `query_log_id` | `bigserial` | 是 | 查询日志编号 |
| `query_log` | `query_name` | `varchar(100)` | 否 | 查询名称 |
| `query_log` | `query_params` | `text` | 否 | 查询参数 |
| `query_log` | `elapsed_ms` | `bigint` | 否 | CHECK `NULL or >= 0` |
| `query_log` | `row_count` | `bigint` | 否 | CHECK `NULL or >= 0` |
| `query_log` | `executed_by` | `bigint` | 否 | FK -> `app_user.user_id` |
| `query_log` | `executed_at` | `timestamp` | 否 | 执行时间 |
| `transaction_log` | `transaction_log_id` | `bigserial` | 是 | 事务日志编号 |
| `transaction_log` | `transaction_id` | `varchar(64)` | 否 | UNIQUE |
| `transaction_log` | `transaction_type` | `varchar(50)` | 否 | CHECK in `new_order/payment` |
| `transaction_log` | `status` | `varchar(20)` | 否 | CHECK in `running/committed/rolled_back/failed` |
| `transaction_log` | `elapsed_ms` | `bigint` | 否 | CHECK `NULL or >= 0` |
| `transaction_log` | `error_message` | `text` | 否 | 错误信息 |
| `transaction_log` | `executed_by` | `bigint` | 否 | FK -> `app_user.user_id` |
| `transaction_log` | `executed_at` | `timestamp` | 否 | 执行时间 |
| `performance_result` | `result_id` | `bigserial` | 是 | 性能结果编号 |
| `performance_result` | `test_name` | `varchar(100)` | 否 | 测试名称 |
| `performance_result` | `test_type` | `varchar(50)` | 否 | 测试类型 |
| `performance_result` | `thread_count` | `integer` | 否 | CHECK `thread_count > 0` |
| `performance_result` | `total_requests` | `integer` | 否 | CHECK `>= 0` |
| `performance_result` | `success_count` | `integer` | 否 | CHECK `>= 0` |
| `performance_result` | `fail_count` | `integer` | 否 | CHECK `>= 0` |
| `performance_result` | `avg_latency_ms` | `numeric(12,2)` | 否 | CHECK `NULL or >= 0` |
| `performance_result` | `max_latency_ms` | `numeric(12,2)` | 否 | CHECK `NULL or >= 0` |
| `performance_result` | `min_latency_ms` | `numeric(12,2)` | 否 | CHECK `NULL or >= 0` |
| `performance_result` | `throughput` | `numeric(12,2)` | 否 | CHECK `NULL or >= 0` |
| `performance_result` | `created_at` | `timestamp` | 否 | 创建时间 |
| `stock_change_log` | `log_id` | `bigserial` | 是 | 库存日志编号 |
| `stock_change_log` | `warehouse_id` | `integer` | 否 | FK with `item_id` -> `stock` |
| `stock_change_log` | `item_id` | `integer` | 否 | FK with `warehouse_id` -> `stock` |
| `stock_change_log` | `old_quantity` | `integer` | 否 | 变更前库存 |
| `stock_change_log` | `new_quantity` | `integer` | 否 | 变更后库存 |
| `stock_change_log` | `change_quantity` | `integer` | 否 | CHECK `change_quantity = new_quantity - old_quantity` |
| `stock_change_log` | `change_type` | `varchar(50)` | 否 | CHECK in `new_order/manual_adjustment/rollback` |
| `stock_change_log` | `related_transaction_id` | `varchar(64)` | 否 | 关联事务编号，不设外键 |
| `stock_change_log` | `changed_at` | `timestamp` | 否 | 变更时间 |

## 6. stock_change_log API 契约

| SQL 字段 | 说明 | API 字段 |
|---|---|---|
| `log_id` | 日志主键 | `logId` |
| `warehouse_id` | 仓库编号 | `warehouseId` |
| `item_id` | 商品编号 | `itemId` |
| `old_quantity` | 变更前库存 | `oldQuantity` |
| `new_quantity` | 变更后库存 | `newQuantity` |
| `change_quantity` | 变化量 | `changeQuantity` |
| `change_type` | 变更类型，例如 `new_order` | `changeType` |
| `related_transaction_id` | 关联事务编号 | `relatedTransactionId` |
| `changed_at` | 变更时间 | `changedAt` |

注意：

```text
related_transaction_id 不设置外键，避免触发器写日志时与 transaction_log 插入顺序强耦合。
```

## 7. COPY 兼容要求

```text
V1 中 TPC-H 字段顺序按 dbgen 输出语义排列。
正式导入模式下，D 应先执行 V1/V2/V3，再 COPY TPC-H 数据，之后执行 V4/V8/V9/V10。
V4 的 ADD CONSTRAINT 脚本已做幂等处理；重复执行同一数据库会跳过已存在约束。
```

> **B 和 D 注意**：`V4__add_constraints.sql` 使用临时 helper 函数检查 `pg_constraint`，已存在约束会自动跳过。
> - B 搭建本地环境时，可重复执行 V4 修复遗漏约束。
> - D 正式导入时，仍遵循 `V1 → V2 → V3 → COPY → V4 → V8 → V9 → V10` 顺序，避免外键在 COPY 前阻塞大批量导入。

## 8. 变更记录要求

每次表字段变更必须记录：

```text
变更日期
变更人
变更表
新增/删除/修改字段
是否影响 B Mapper/DTO
是否影响 D COPY 或统计脚本
```
