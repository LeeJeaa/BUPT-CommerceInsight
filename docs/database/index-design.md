# 索引设计规范

## 1. 目标

索引用于支撑 TPC-H 查询、业务查询和 TPC-C 事务，不追求无限制加索引。每个索引必须说明服务的查询或事务。

## 2. 基线索引

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

## 3. 对比实验

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

