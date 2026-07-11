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
avgQuantity
countOrder
```

推荐图表：分组表格、汇总柱状图。

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

## 5. Q14 促销效果

参数：

```text
month
```

输出字段：

```text
promoRevenue
```

推荐图表：饼图、仪表盘或指标卡。

