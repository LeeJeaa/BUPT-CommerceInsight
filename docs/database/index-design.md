# 索引设计规范

## 1. 目标

索引用于支撑 TPC-H 查询、业务查询和 TPC-C 事务，不追求无限制加索引。每个索引必须说明服务的查询或事务。

## 2. 基线索引

```sql
CREATE INDEX idx_lineitem_returnflag_linestatus ON lineitem(l_returnflag, l_linestatus);
CREATE INDEX idx_orders_orderdate ON orders(o_orderdate);
CREATE INDEX idx_orders_custkey ON orders(o_custkey);
CREATE INDEX idx_lineitem_orderkey ON lineitem(l_orderkey);
CREATE INDEX idx_lineitem_shipdate ON lineitem(l_shipdate);
CREATE INDEX idx_lineitem_shipmode_receiptdate ON lineitem(l_shipmode, l_receiptdate);
CREATE INDEX idx_customer_nationkey ON customer(c_nationkey);
CREATE INDEX idx_supplier_nationkey ON supplier(s_nationkey);
CREATE INDEX idx_partsupp_suppkey ON partsupp(ps_suppkey);
CREATE INDEX idx_partsupp_partkey ON partsupp(ps_partkey);
CREATE INDEX idx_transaction_log_type_time ON transaction_log(transaction_type, executed_at);
CREATE INDEX idx_stock_change_log_item_time ON stock_change_log(warehouse_id, item_id, changed_at);
```

SQL 资产位置：

```text
sql/V10__indexes_baseline.sql
```

## 2.1 索引理由

| 索引 | 服务对象 | 设计理由 |
|---|---|---|
| `idx_lineitem_returnflag_linestatus` | Q1 | Q1 按 `l_returnflag, l_linestatus` 分组并排序；复合索引提供已排序输入，正式数据量大时可消除 Sort 节点，将 GroupAggregate 的排序开销前移到索引扫描 |
| `idx_orders_orderdate` | Q5、订单收入业务查询 | 支撑 `orders.o_orderdate` 日期范围过滤 |
| `idx_orders_custkey` | Q5、客户订单查询 | 支撑 `orders` 与 `customer` 的连接 |
| `idx_lineitem_orderkey` | Q5、Q12、订单收入业务查询 | 支撑 `lineitem` 与 `orders` 的连接 |
| `idx_lineitem_shipdate` | Q1、Q14 | 支撑 `lineitem.l_shipdate` 日期过滤 |
| `idx_lineitem_shipmode_receiptdate` | Q12 | 支撑发运方式和收货日期组合过滤 |
| `idx_customer_nationkey` | Q5 | 支撑 `customer` 与 `nation` 的连接 |
| `idx_supplier_nationkey` | Q5 | 支撑 `supplier` 与 `nation` 的连接 |
| `idx_partsupp_suppkey` | 零部件供应查询 | 支撑按供应商查询供应关系 |
| `idx_partsupp_partkey` | 零部件供应查询 | 支撑按零部件查询供应关系 |
| `idx_transaction_log_type_time` | TPC-C 事务日志页面、性能检查 | 支撑按事务类型和执行时间查询 |
| `idx_stock_change_log_item_time` | 库存变动审计页面 | 支撑按仓库、商品和时间查看库存变化 |

## 3. 对比实验

正式导入时点：

```text
V10 必须在 COPY 和 V4 约束校验之后执行。
idx_orders_orderdate、idx_orders_custkey、idx_lineitem_orderkey、idx_lineitem_shipdate、idx_lineitem_shipmode_receiptdate 等大表索引会拖慢正式 COPY 期间的写入。
D 做 SF=0.1/SF=0.6/SF=1 导入时，应坚持 V1/V2/V3 -> COPY -> V4/V8/V9 -> V10 的顺序。
```

D 在正式数据上保存：

```text
无索引耗时
有索引耗时
EXPLAIN ANALYZE 前后计划
线程数变化下的平均延迟和吞吐量
```

A 提供：

```text
每个索引的设计理由
是否命中查询过滤、连接或排序字段
执行计划变化解释
```

## 4. EXPLAIN 观察点

```text
Q1：观察 lineitem 的扫描方式、GroupAggregate/HashAggregate、Sort。
Q5：观察 orders 日期过滤、customer/orders/lineitem/supplier/nation/region 连接方式。
Q12：观察 idx_lineitem_shipmode_receiptdate 是否有机会服务 shipmode + receiptdate 过滤。
Q14：观察 lineitem shipdate 过滤和 part 连接方式。
```

说明：

```text
小样例数据下 PostgreSQL 可能仍选择 Seq Scan，这是正常现象。
正式结论必须以 D 在 SF=0.1/SF=0.6/SF=1 上的 EXPLAIN ANALYZE 为准。
```

