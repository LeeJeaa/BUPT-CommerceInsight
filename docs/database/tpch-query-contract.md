# TPC-H 查询契约

## 1. 通用输出

每个查询必须返回：

```text
queryName
elapsedMs
rowCount
records
chartData
explainPlan 可选
```

## 2. Q1 定价汇总报表

参数：

```text
shipDate
```

输出字段：

```text
returnFlag
lineStatus
sumQuantity
sumBasePrice
sumDiscountedPrice
sumCharge
avgQuantity
avgPrice
avgDisc
countOrder
```

推荐图表：分组表格、汇总柱状图。

SQL 资产位置：

```text
sql/V5__tpch_queries.sql
PREPARE tpch_q1(date)
```

SQL 输出别名与 API 字段映射：

| SQL 输出别名 | API 字段 |
|---|---|
| `return_flag` | `returnFlag` |
| `line_status` | `lineStatus` |
| `sum_quantity` | `sumQuantity` |
| `sum_base_price` | `sumBasePrice` |
| `sum_discounted_price` | `sumDiscountedPrice` |
| `sum_charge` | `sumCharge` |
| `avg_quantity` | `avgQuantity` |
| `avg_price` | `avgPrice` |
| `avg_disc` | `avgDisc` |
| `count_order` | `countOrder` |

## 3. Q5 本地供应商收入

参数：

```text
regionName
startDate
endDate
```

输出字段：

```text
nationName
revenue
```

推荐图表：国家收入柱状图。

SQL 资产位置：

```text
sql/V5__tpch_queries.sql
PREPARE tpch_q5(varchar, date, date)
```

SQL 输出别名与 API 字段映射：

| SQL 输出别名 | API 字段 |
|---|---|
| `nation_name` | `nationName` |
| `revenue` | `revenue` |

## 4. Q12 运送方式和订单优先级

参数：

```text
shipMode1
shipMode2
startDate
endDate
```

输出字段：

```text
shipMode
highLineCount
lowLineCount
```

推荐图表：堆叠柱状图。

SQL 资产位置：

```text
sql/V5__tpch_queries.sql
PREPARE tpch_q12(varchar, varchar, date, date)
```

SQL 输出别名与 API 字段映射：

| SQL 输出别名 | API 字段 |
|---|---|
| `ship_mode` | `shipMode` |
| `high_line_count` | `highLineCount` |
| `low_line_count` | `lowLineCount` |

## 5. Q14 促销效果

参数：

```text
month
```

输出字段：

```text
promoRevenuePercent
```

推荐图表：饼图、仪表盘或指标卡。

SQL 资产位置：

```text
sql/V5__tpch_queries.sql
PREPARE tpch_q14(date)
```

SQL 输出别名与 API 字段映射：

| SQL 输出别名 | API 字段 |
|---|---|
| `promo_revenue_percent` | `promoRevenuePercent` |

## 6. A/B 对接说明

```text
A 的 SQL 输出别名优先使用 snake_case。
B 必须在 DTO/VO 或 Mapper ResultMap 中映射为 API 契约要求的 lowerCamelCase，不能把 snake_case 直接透出给 C。
C 不直接读取 SQL 输出别名，也不依赖数据库字段。
```

本地样例验证：

```sql
EXECUTE tpch_q1(DATE '1995-12-31');
EXECUTE tpch_q5('ASIA', DATE '1994-01-01', DATE '1995-01-01');
EXECUTE tpch_q12('MAIL', 'SHIP', DATE '1994-01-01', DATE '1995-01-01');
EXECUTE tpch_q14(DATE '1995-09-01');
```

