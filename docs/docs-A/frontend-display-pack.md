# 前端数据展示包（A → C）

> 适用对象：成员 C（Vue 3 + Element Plus + ECharts）  
> 目的：C 可直接复制本文档中的 Mock JSON 和 ECharts 配置，无需反查数据库字段或等待 B 接口就绪。

---

## 1. 字段来源声明

C 只依赖 API JSON 字段（`lowerCamelCase`），不读取数据库字段。  
本文档字段全部来自 `docs/api/mock-contract.md`，与 B 的接口返回保持一致。

---

## 2. 通用响应包装

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

错误响应：

```json
{
  "code": 400,
  "message": "参数错误：shipDate 不能为空",
  "data": null
}
```

---

## 3. TPC-H Mock JSON（可直接粘贴到 `frontend/src/mock/tpch.mock.js`）

### 3.1 Q1 定价汇总

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "queryName": "TPC-H Q1 定价汇总报表",
    "elapsedMs": 820,
    "rowCount": 4,
    "records": [
      {
        "returnFlag": "A",
        "lineStatus": "F",
        "sumQuantity": 37734107.00,
        "sumBasePrice": 56586554400.73,
        "sumDiscountedPrice": 53758257134.87,
        "sumCharge": 55909065222.83,
        "avgQuantity": 25.52,
        "avgPrice": 38273.13,
        "avgDisc": 0.05,
        "countOrder": 1478493
      },
      {
        "returnFlag": "N",
        "lineStatus": "F",
        "sumQuantity": 991417.00,
        "sumBasePrice": 1487504710.38,
        "sumDiscountedPrice": 1413082168.05,
        "sumCharge": 1469649223.19,
        "avgQuantity": 25.52,
        "avgPrice": 38284.47,
        "avgDisc": 0.05,
        "countOrder": 38854
      },
      {
        "returnFlag": "N",
        "lineStatus": "O",
        "sumQuantity": 74476040.00,
        "sumBasePrice": 111701729697.74,
        "sumDiscountedPrice": 106118230307.61,
        "sumCharge": 110367043872.50,
        "avgQuantity": 25.50,
        "avgPrice": 38249.12,
        "avgDisc": 0.05,
        "countOrder": 2920374
      },
      {
        "returnFlag": "R",
        "lineStatus": "F",
        "sumQuantity": 37719753.00,
        "sumBasePrice": 56568041380.90,
        "sumDiscountedPrice": 53741292684.60,
        "sumCharge": 55889619119.83,
        "avgQuantity": 25.51,
        "avgPrice": 38250.85,
        "avgDisc": 0.05,
        "countOrder": 1478870
      }
    ],
    "chartData": {
      "xAxis": ["A-F", "N-F", "N-O", "R-F"],
      "series": [55909065222.83, 1469649223.19, 110367043872.50, 55889619119.83]
    }
  }
}
```

---

### 3.2 Q5 本地供应商收入

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "queryName": "TPC-H Q5 本地供应商收入分析",
    "elapsedMs": 3521,
    "rowCount": 5,
    "records": [
      { "nationName": "CHINA",    "revenue": 9234567.89 },
      { "nationName": "INDIA",    "revenue": 8765432.10 },
      { "nationName": "JAPAN",    "revenue": 7654321.11 },
      { "nationName": "INDONESIA","revenue": 6543210.22 },
      { "nationName": "VIETNAM",  "revenue": 5432109.33 }
    ],
    "chartData": {
      "xAxis": ["CHINA", "INDIA", "JAPAN", "INDONESIA", "VIETNAM"],
      "series": [9234567.89, 8765432.10, 7654321.11, 6543210.22, 5432109.33]
    }
  }
}
```

---

### 3.3 Q12 运送方式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "queryName": "TPC-H Q12 运送方式与订单优先级",
    "elapsedMs": 1240,
    "rowCount": 2,
    "records": [
      { "shipMode": "MAIL", "highLineCount": 6202,  "lowLineCount": 9324 },
      { "shipMode": "SHIP", "highLineCount": 6200,  "lowLineCount": 9262 }
    ],
    "chartData": {
      "xAxis": ["MAIL", "SHIP"],
      "highSeries": [6202, 6200],
      "lowSeries":  [9324, 9262]
    }
  }
}
```

---

### 3.4 Q14 促销效果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "queryName": "TPC-H Q14 促销收入占比",
    "elapsedMs": 680,
    "rowCount": 1,
    "records": [
      { "promoRevenuePercent": 16.38 }
    ],
    "chartData": {
      "gauge": 16.38,
      "pieData": [
        { "name": "促销收入", "value": 16.38 },
        { "name": "非促销收入", "value": 83.62 }
      ]
    }
  }
}
```

---

## 4. TPC-C Mock JSON（`frontend/src/mock/tpcc.mock.js`）

### 4.1 New-Order 成功

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "transactionId": "no-demo-001",
    "transactionType": "new_order",
    "status": "committed",
    "orderId": 3001,
    "warehouseId": 1,
    "districtId": 1,
    "customerId": 1,
    "elapsedMs": 45,
    "items": [
      { "itemId": 1001, "itemName": "Item-1001", "quantity": 2, "amount": 50.20 }
    ]
  }
}
```

### 4.2 New-Order 失败（库存不足）

```json
{
  "code": 500,
  "message": "库存不足，事务已回滚",
  "data": {
    "transactionId": "no-demo-rollback-001",
    "transactionType": "new_order",
    "status": "failed",
    "elapsedMs": 12
  }
}
```

### 4.3 Payment 成功

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "transactionId": "pay-demo-001",
    "transactionType": "payment",
    "status": "committed",
    "customerId": 1,
    "newBalance": 974.50,
    "amount": 25.00,
    "elapsedMs": 38
  }
}
```

---

## 5. 导入导出 Mock JSON

### 5.1 发起导入任务

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 1001,
    "tableName": "orders",
    "fileName": "orders_sample.txt",
    "status": "running"
  }
}
```

### 5.2 查询导入进度

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 1001,
    "tableName": "orders",
    "fileName": "orders_sample.txt",
    "status": "success",
    "totalRows": 1000,
    "successRows": 990,
    "failedRows": 10,
    "elapsedMs": 1250,
    "startedAt": "2026-07-06 10:00:00",
    "endedAt": "2026-07-06 10:00:02"
  }
}
```

### 5.3 错误日志（分页）

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "pageNo": 1,
    "pageSize": 20,
    "total": 10,
    "records": [
      {
        "lineNumber": 18,
        "fieldName": "o_totalprice",
        "fieldValue": "-1",
        "errorReason": "金额不能为负数"
      },
      {
        "lineNumber": 42,
        "fieldName": "o_orderdate",
        "fieldValue": "99-13-01",
        "errorReason": "日期格式错误"
      }
    ]
  }
}
```

---

## 6. 性能测试结果 Mock（`frontend/src/mock/performance.mock.js`）

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "testName": "TPC-H Concurrent Query Test",
    "threadCount": 8,
    "totalRequests": 80,
    "successCount": 80,
    "failCount": 0,
    "avgLatencyMs": 1260.5,
    "maxLatencyMs": 2890.2,
    "minLatencyMs": 330.1,
    "throughput": 6.35,
    "records": [
      { "threadCount": 1, "avgLatencyMs": 410.2,  "throughput": 2.43 },
      { "threadCount": 2, "avgLatencyMs": 590.8,  "throughput": 3.38 },
      { "threadCount": 4, "avgLatencyMs": 910.4,  "throughput": 4.52 },
      { "threadCount": 8, "avgLatencyMs": 1260.5, "throughput": 6.35 }
    ],
    "chartData": {
      "xAxis": [1, 2, 4, 8],
      "latencySeries":    [410.2, 590.8, 910.4, 1260.5],
      "throughputSeries": [2.43,  3.38,  4.52,  6.35]
    }
  }
}
```

---

## 7. ECharts 配置参考

### 7.1 Q5 国家收入柱状图

```javascript
const q5Option = {
  title: { text: 'Q5 本地供应商收入（亚洲区）' },
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: data.chartData.xAxis          // ["CHINA","INDIA",...]
  },
  yAxis: { type: 'value', name: '收入（元）' },
  series: [{
    name: '收入',
    type: 'bar',
    data: data.chartData.series,        // [9234567.89, ...]
    itemStyle: { color: '#5470c6' }
  }]
}
```

### 7.2 Q12 堆叠柱状图

```javascript
const q12Option = {
  title: { text: 'Q12 运送方式优先级分布' },
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  legend: { data: ['高优先级', '低优先级'] },
  xAxis: { type: 'category', data: data.chartData.xAxis },
  yAxis: { type: 'value' },
  series: [
    { name: '高优先级', type: 'bar', stack: 'total', data: data.chartData.highSeries },
    { name: '低优先级', type: 'bar', stack: 'total', data: data.chartData.lowSeries }
  ]
}
```

### 7.3 Q14 促销占比饼图

```javascript
const q14Option = {
  title: { text: 'Q14 促销收入占比', left: 'center' },
  tooltip: { trigger: 'item', formatter: '{b}: {d}%' },
  series: [{
    name: '收入占比',
    type: 'pie',
    radius: '60%',
    data: data.chartData.pieData        // [{name:'促销收入',value:16.38},...]
  }]
}
```

### 7.4 并发测试折线图（延迟 + 吞吐量双轴）

```javascript
const perfOption = {
  title: { text: 'TPC-H 并发性能' },
  tooltip: { trigger: 'axis' },
  legend: { data: ['平均延迟(ms)', '吞吐量(QPS)'] },
  xAxis: { type: 'category', name: '并发线程数', data: data.chartData.xAxis },
  yAxis: [
    { type: 'value', name: '延迟(ms)' },
    { type: 'value', name: '吞吐量(QPS)', position: 'right' }
  ],
  series: [
    { name: '平均延迟(ms)', type: 'line', yAxisIndex: 0, data: data.chartData.latencySeries },
    { name: '吞吐量(QPS)', type: 'bar',  yAxisIndex: 1, data: data.chartData.throughputSeries }
  ]
}
```

---

## 8. Element Plus 表格字段对照

| 接口 | `prop` 值 | `label` 建议 |
|---|---|---|
| Q1 | `returnFlag` | 退货标志 |
| Q1 | `lineStatus` | 明细状态 |
| Q1 | `sumCharge` | 总费用 |
| Q1 | `avgPrice` | 平均单价 |
| Q1 | `countOrder` | 订单行数 |
| Q5 | `nationName` | 国家 |
| Q5 | `revenue` | 收入 |
| Q12 | `shipMode` | 运输方式 |
| Q12 | `highLineCount` | 高优先级行数 |
| Q12 | `lowLineCount` | 低优先级行数 |
| Q14 | `promoRevenuePercent` | 促销收入占比(%) |
| 导入 | `lineNumber` | 行号 |
| 导入 | `fieldName` | 字段名 |
| 导入 | `fieldValue` | 原始值 |
| 导入 | `errorReason` | 错误原因 |
