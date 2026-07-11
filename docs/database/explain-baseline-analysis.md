# EXPLAIN 基线分析规范

## 1. 输出位置

```text
report/explain_plans/
```

建议文件：

```text
q1_without_index.txt
q1_with_index.txt
q5_without_index.txt
q5_with_index.txt
q12_without_index.txt
q12_with_index.txt
q14_without_index.txt
q14_with_index.txt
```

SQL 资产位置：

```text
sql/V12__explain_baseline.sql
```

## 2. 记录字段

```text
查询名称
数据规模
是否有索引
执行时间
扫描方式
连接方式
排序或聚合节点
主要瓶颈
优化结论
```

## 3. 分析口径

```text
不要只贴计划，要解释计划。
重点观察 Seq Scan、Index Scan、Hash Join、Nested Loop、Sort、Aggregate。
性能结论必须对应具体数据规模和执行时间。
```

## 4. A 提供给 D 的执行模板

D 可在正式数据上执行：

```bash
psql -d tpc_commerce -v ON_ERROR_STOP=1 -f sql/V12__explain_baseline.sql
```

建议保存方式：

```bash
psql -d tpc_commerce -f sql/V12__explain_baseline.sql > report/explain_plans/tpch_baseline_with_index.txt
```

如果要做无索引对比，应在执行 `V10__indexes_baseline.sql` 前先保存一次结果，再执行 `V10` 后保存一次结果。

## 5. A 的解读模板

每条查询在报告中按以下格式解释：

```text
查询名称：
数据规模：
索引状态：
执行时间：
主要扫描方式：
主要连接方式：
聚合或排序节点：
瓶颈：
索引是否命中：
结论：
```

注意：

```text
样例数据上的 EXPLAIN 只用于确认 SQL 可执行，不作为性能结论。
正式性能结论由 D 的正式数据和 EXPLAIN ANALYZE 支撑，A 负责解释原因。
```

## 5.1 无索引 vs 有索引对比操作步骤

D 在正式数据上执行对比实验时，按以下顺序保存两份计划：

```bash
# 步骤1：在执行 V10 之前，保存无索引基线
psql -d tpc_commerce -f sql/V12__explain_baseline.sql \
  > report/explain_plans/tpch_baseline_without_index.txt

# 步骤2：执行 V10 创建基线索引
psql -d tpc_commerce -f sql/V10__indexes_baseline.sql

# 步骤3：保存有索引计划
psql -d tpc_commerce -f sql/V12__explain_baseline.sql \
  > report/explain_plans/tpch_baseline_with_index.txt
```

对比记录模板（D 按查询填写）：

| 查询 | 数据规模 | 无索引耗时(ms) | 有索引耗时(ms) | 无索引扫描方式 | 有索引扫描方式 | 结论 |
|---|---|---|---|---|---|---|
| Q1 | SF=0.1 | | | Seq Scan | Index Scan / GroupAggregate | |
| Q5 | SF=0.1 | | | Seq Scan | Index Scan (orderdate, custkey) | |
| Q12 | SF=0.1 | | | Seq Scan | Index Scan (shipmode+receiptdate) | |
| Q14 | SF=0.1 | | | Seq Scan | Index Scan (shipdate) | |

注意事项：

```text
- 每次对比前执行 ANALYZE 以更新统计信息：psql -c "ANALYZE;"
- 小样例数据（V11）下 PostgreSQL 通常仍选 Seq Scan，对比无意义，必须使用 SF=0.1 以上。
- 对比结果应写入 report/parts/D_性能测试与分析.md，A 负责协助解读执行计划差异。
```

## 6. 样例计划摘录

以下摘录来自 PostgreSQL 16 开发样例数据，用途是证明 `V12__explain_baseline.sql` 可执行，并展示解读口径。样例数据量很小，不能作为真实性能结论。

### 6.1 Q1 定价汇总

```text
GroupAggregate
  Group Key: l_returnflag, l_linestatus
  -> Sort
       Sort Key: l_returnflag, l_linestatus
       -> Seq Scan on lineitem
            Filter: l_shipdate <= '1995-12-31'
```

解读：

```text
Q1 需要扫描满足日期条件的 lineitem，再按 return_flag 和 line_status 聚合。
小样例下 Seq Scan 正常；正式数据上应观察 idx_lineitem_shipdate 是否能减少日期过滤扫描量。
Sort + GroupAggregate 表明当前计划先排序再分组，正式数据上也可能出现 HashAggregate。
```

### 6.2 Q5 本地供应商收入

```text
Nested Loop
  -> Index Scan using idx_orders_orderdate on orders
       Index Cond: o_orderdate >= '1994-01-01' AND o_orderdate < '1995-01-01'
  -> Index Scan using idx_lineitem_orderkey on lineitem
  -> Index Scan using idx_customer_nationkey on customer
```

解读：

```text
Q5 已命中 orders 日期索引和 lineitem/order 连接索引。
正式数据上应重点比较 Hash Join 与 Nested Loop 的选择，以及 customer/supplier/nation/region 连接是否产生过大中间结果。
```

### 6.3 Q12 运送方式和订单优先级

```text
GroupAggregate
  -> Sort
       Sort Key: trim(l_shipmode)
       -> Index Scan using idx_lineitem_shipmode_receiptdate on lineitem
            Index Cond: l_shipmode IN ('MAIL','SHIP') AND receiptdate range
```

解读：

```text
Q12 的组合索引服务 shipmode + receiptdate 过滤。
仍需要检查 commitdate/shipdate 的过滤比例；正式数据上如果过滤后行数仍大，可能需要重新评估组合索引顺序。
```

### 6.4 Q14 促销收入百分比

```text
Aggregate
  -> Nested Loop
       -> Index Scan using idx_lineitem_shipdate on lineitem
            Index Cond: l_shipdate >= '1995-09-01' AND l_shipdate < '1995-10-01'
       -> Index Scan using pk_part on part
```

解读：

```text
Q14 主要依赖 lineitem 的 shipdate 月份过滤，然后连接 part 判断 p_type 是否为 PROMO。
输出字段为 promo_revenue_percent，语义是促销收入占当月总收入的百分比。
```

