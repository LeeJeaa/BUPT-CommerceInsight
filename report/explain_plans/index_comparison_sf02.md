# TPC-H SF0.2 索引前后 EXPLAIN 对比记录

> 记录用途：D 成员实验过程证据索引，不是最终报告正文。  
> 数据来源：课程提供的 `tpc-h数据(2)`，导入后 `lineitem=1199969`、`orders=300000`。  
> 参数说明：课程数据日期范围为 2015-2021，因此本次 EXPLAIN 使用数据集内有效日期，避免经典 TPC-H 1994/1995/1998 参数查空。

## 1. 输入文件

无索引：

```text
report/explain_plans/tpch_explain_without_index_sf0.2_20260708_213609.txt
```

有索引：

```text
report/explain_plans/tpch_explain_with_index_sf0.2_20260708_213627.txt
```

## 2. 查询参数

| 查询 | 参数 |
|---|---|
| Q1 | `shipDate=2020-12-31` |
| Q5 | `regionName=AFRICA`, `startDate=2020-01-01`, `endDate=2021-01-01` |
| Q12 | `shipMode1=MAIL`, `shipMode2=SHIP`, `startDate=2020-01-01`, `endDate=2021-01-01` |
| Q14 | `month=2020-09-01` |

## 3. 执行时间对比

| 查询 | 无 V10 基线索引耗时(ms) | 有 V10 基线索引耗时(ms) | 观察 |
|---|---:|---:|---|
| Q1 | 235.357 | 229.764 | 仍为 Parallel Seq Scan，日期条件覆盖大部分 `lineitem`，索引收益不明显 |
| Q5 | 70.308 | 79.030 | 命中 `idx_orders_orderdate` 和 `idx_lineitem_orderkey`，但本次数据规模下整体略慢 |
| Q12 | 61.079 | 38.727 | 命中 `idx_lineitem_shipmode_receiptdate`，耗时下降明显 |
| Q14 | 41.230 | 18.636 | 命中 `idx_lineitem_shipdate`，耗时下降明显 |

## 4. 截图证据

```text
report/screenshots/20260708_213611_explain_without_index_sf02.png
report/screenshots/20260708_213629_explain_with_index_sf02.png
```

## 5. 待报告阶段展开

- Q1 因过滤选择性低，PostgreSQL 选择并行顺序扫描是合理结果。
- Q5 虽命中日期和连接索引，但小规模数据与缓存状态会影响耗时，需要报告中避免过度宣称“索引必然更快”。
- Q12/Q14 可以作为索引优化正向证据。
