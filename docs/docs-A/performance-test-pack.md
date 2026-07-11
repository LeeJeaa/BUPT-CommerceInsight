# 性能实验支持包（A → D）

> 适用对象：成员 D（Docker PostgreSQL、EXPLAIN、索引对比、压测）  
> 目的：D 可直接复制本文档中的 EXPLAIN 命令、索引对比实验步骤和压测端点说明，无需反查 SQL 原文。

---

## 1. 正式 EXPLAIN 执行流程

### 1.1 实验顺序

```text
第1步：启动 Docker PostgreSQL，执行正式 TPC-H 数据导入（SF=1）
第2步：不执行 V10 的情况下，对 Q1/Q5/Q12/Q14 运行 EXPLAIN ANALYZE（无索引基线）
第3步：执行 sql/V10__indexes_baseline.sql，建立初版索引
第4步：再次对 Q1/Q5/Q12/Q14 运行 EXPLAIN ANALYZE（有索引对比）
第5步：保存两组执行计划到 report/explain_plans/
```

### 1.2 进入容器

```bash
docker exec -it tpc-commerce-postgres psql -U tpc_admin -d tpc_commerce
```

---

## 2. EXPLAIN ANALYZE 命令（可直接粘贴）

### 2.1 Q1 定价汇总

```sql
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT
    l_returnflag,
    l_linestatus,
    SUM(l_quantity) AS sum_quantity,
    SUM(l_extendedprice) AS sum_base_price,
    SUM(l_extendedprice * (1 - l_discount)) AS sum_discounted_price,
    SUM(l_extendedprice * (1 - l_discount) * (1 + l_tax)) AS sum_charge,
    AVG(l_quantity) AS avg_quantity,
    AVG(l_extendedprice) AS avg_price,
    AVG(l_discount) AS avg_disc,
    COUNT(*) AS count_order
FROM lineitem
WHERE l_shipdate <= '1998-09-01'
GROUP BY l_returnflag, l_linestatus
ORDER BY l_returnflag, l_linestatus;
```

保存路径：`report/explain_plans/q1_no_index.txt` → `report/explain_plans/q1_with_index.txt`

---

### 2.2 Q5 本地供应商收入

```sql
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT
    TRIM(n.n_name) AS nation_name,
    SUM(l.l_extendedprice * (1 - l.l_discount)) AS revenue
FROM customer c
JOIN orders o ON c.c_custkey = o.o_custkey
JOIN lineitem l ON l.l_orderkey = o.o_orderkey
JOIN supplier s ON s.s_suppkey = l.l_suppkey
JOIN nation n
    ON n.n_nationkey = c.c_nationkey
   AND n.n_nationkey = s.s_nationkey
JOIN region r ON r.r_regionkey = n.n_regionkey
WHERE r.r_name = 'ASIA'
  AND o.o_orderdate >= '1994-01-01'
  AND o.o_orderdate < '1995-01-01'
GROUP BY n.n_name
ORDER BY revenue DESC;
```

保存路径：`report/explain_plans/q5_no_index.txt` → `report/explain_plans/q5_with_index.txt`

---

### 2.3 Q12 运送方式

```sql
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT
    TRIM(l.l_shipmode) AS ship_mode,
    SUM(CASE WHEN o.o_orderpriority IN ('1-URGENT','2-HIGH') THEN 1 ELSE 0 END) AS high_line_count,
    SUM(CASE WHEN o.o_orderpriority NOT IN ('1-URGENT','2-HIGH') THEN 1 ELSE 0 END) AS low_line_count
FROM orders o
JOIN lineitem l ON o.o_orderkey = l.l_orderkey
WHERE l.l_shipmode IN ('MAIL','SHIP')
  AND l.l_commitdate < l.l_receiptdate
  AND l.l_shipdate   < l.l_commitdate
  AND l.l_receiptdate >= '1994-01-01'
  AND l.l_receiptdate  < '1995-01-01'
GROUP BY TRIM(l.l_shipmode)
ORDER BY TRIM(l.l_shipmode);
```

保存路径：`report/explain_plans/q12_no_index.txt` → `report/explain_plans/q12_with_index.txt`

---

### 2.4 Q14 促销效果

```sql
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT
    100.00 * SUM(CASE WHEN p.p_type LIKE 'PROMO%'
                      THEN l.l_extendedprice * (1 - l.l_discount)
                      ELSE 0 END)
    / NULLIF(SUM(l.l_extendedprice * (1 - l.l_discount)), 0)
    AS promo_revenue_percent
FROM lineitem l
JOIN part p ON p.p_partkey = l.l_partkey
WHERE l.l_shipdate >= '1995-09-01'
  AND l.l_shipdate  < '1995-10-01';
```

保存路径：`report/explain_plans/q14_no_index.txt` → `report/explain_plans/q14_with_index.txt`

---

## 3. 索引建立脚本（sql/V10 内容摘要）

执行命令：

```bash
\i /sql/V10__indexes_baseline.sql
```

包含以下索引：

```sql
CREATE INDEX idx_lineitem_shipdate        ON lineitem(l_shipdate);
CREATE INDEX idx_lineitem_orderkey        ON lineitem(l_orderkey);
CREATE INDEX idx_lineitem_shipmode_recpt  ON lineitem(l_shipmode, l_receiptdate);
CREATE INDEX idx_orders_orderdate         ON orders(o_orderdate);
CREATE INDEX idx_orders_custkey           ON orders(o_custkey);
CREATE INDEX idx_customer_nationkey       ON customer(c_nationkey);
CREATE INDEX idx_supplier_nationkey       ON supplier(s_nationkey);
CREATE INDEX idx_partsupp_suppkey         ON partsupp(ps_suppkey);
CREATE INDEX idx_partsupp_partkey         ON partsupp(ps_partkey);
```

---

## 4. 行数统计脚本

> 正式导入完成后，D 运行以下查询并截图保存。

```sql
SELECT 'region'        AS table_name, COUNT(*) AS row_count FROM region
UNION ALL
SELECT 'nation',       COUNT(*) FROM nation
UNION ALL
SELECT 'supplier',     COUNT(*) FROM supplier
UNION ALL
SELECT 'part',         COUNT(*) FROM part
UNION ALL
SELECT 'partsupp',     COUNT(*) FROM partsupp
UNION ALL
SELECT 'customer',     COUNT(*) FROM customer
UNION ALL
SELECT 'orders',       COUNT(*) FROM orders
UNION ALL
SELECT 'lineitem',     COUNT(*) FROM lineitem
UNION ALL
SELECT 'warehouse',    COUNT(*) FROM warehouse
UNION ALL
SELECT 'district',     COUNT(*) FROM district
UNION ALL
SELECT 'tpcc_customer',COUNT(*) FROM tpcc_customer
UNION ALL
SELECT 'item',         COUNT(*) FROM item
UNION ALL
SELECT 'stock',        COUNT(*) FROM stock
ORDER BY table_name;
```

SF=0.1 参考行数：

| 表 | SF=0.1 参考行数 |
|---|---|
| `region` | 5 |
| `nation` | 25 |
| `supplier` | 1,000 |
| `part` | 20,000 |
| `partsupp` | 80,000 |
| `customer` | 15,000 |
| `orders` | 150,000 |
| `lineitem` | ~600,000 |

---

## 5. 压测端点说明（D 编写 Python 脚本时使用）

### 5.1 TPC-H 并发查询测试端点

| 接口 | Method | 路径 | 典型参数 |
|---|---|---|---|
| Q1 | GET | `/api/tpch/q1` | `?shipDate=1998-09-01` |
| Q5 | GET | `/api/tpch/q5` | `?regionName=ASIA&startDate=1994-01-01&endDate=1995-01-01` |
| Q12 | GET | `/api/tpch/q12` | `?shipMode1=MAIL&shipMode2=SHIP&startDate=1994-01-01&endDate=1995-01-01` |
| Q14 | GET | `/api/tpch/q14` | `?month=1995-09-01` |

鉴权：登录后获取 token，请求头加 `Authorization: Bearer <token>`。

### 5.2 TPC-C 并发事务测试端点

| 事务 | Method | 路径 | Body |
|---|---|---|---|
| New-Order | POST | `/api/tpcc/new-order` | 见下 |
| Payment   | POST | `/api/tpcc/payment`   | 见下 |

New-Order 请求体：

```json
{
  "warehouseId": 1,
  "districtId":  1,
  "customerId":  1,
  "items": [{ "itemId": 1001, "quantity": 1 }]
}
```

Payment 请求体：

```json
{
  "warehouseId": 1,
  "districtId":  1,
  "customerId":  1,
  "amount":      25.00
}
```

### 5.3 Python 脚本测试指标

D 的 Python 压测脚本输出 CSV 需包含以下字段（与 `performance_result` 表一致）：

```text
thread_count, total_requests, success_count, fail_count,
avg_latency_ms, max_latency_ms, min_latency_ms, throughput
```

---

## 6. 索引前后对比记录模板

D 将以下结果填入 `report/explain_plans/index_comparison.md`：

| 查询 | 无索引耗时(ms) | 有索引耗时(ms) | 加速比 | 主要生效索引 |
|---|---|---|---|---|
| Q1 | — | — | — | `idx_lineitem_shipdate` |
| Q5 | — | — | — | `idx_orders_orderdate`, `idx_customer_nationkey` |
| Q12 | — | — | — | `idx_lineitem_shipmode_recpt` |
| Q14 | — | — | — | `idx_lineitem_shipdate` |

> 耗时从 `EXPLAIN ANALYZE` 输出的 `Execution Time:` 行读取，单位 ms。

---

## 7. A 提供的 EXPLAIN 解读要点

D 执行完后，把执行计划文本发给 A，A 负责解读以下内容并补充报告章节：

- 各查询使用了哪些扫描类型（Seq Scan / Index Scan / Bitmap Index Scan）
- Join 方式（Hash Join / Merge Join / Nested Loop）
- 索引生效的依据（实际 rows、actual time）
- 未生效的索引原因（数据量太小 / 选择性不足 / 统计信息未更新）
- 建议 D 在 SF=1 数据上执行 `ANALYZE` 刷新统计信息后再跑

```sql
-- 刷新统计信息（建议在 COPY 完成后执行）
ANALYZE lineitem;
ANALYZE orders;
ANALYZE customer;
ANALYZE supplier;
ANALYZE part;
ANALYZE partsupp;
```
