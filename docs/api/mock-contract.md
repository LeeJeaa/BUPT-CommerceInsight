# Mock 契约

## 1. 使用原则

```text
B 的 Mock Repository 和 C 的 Mock JSON 只能使用本文字段。
C 只依赖 Mock/API 字段，不读取数据库字段。
真实接口上线后，字段必须与 Mock 保持一致。
```

## 2. 通用包装

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 3. 通用分页字段

```json
{
  "pageNo": 1,
  "pageSize": 20,
  "total": 100,
  "records": []
}
```

## 4. 用户 Mock

```json
{
  "userId": 1,
  "username": "admin",
  "realName": "管理员",
  "email": "admin@example.com",
  "role": "admin",
  "status": "approved",
  "createdAt": "2026-07-06 10:00:00"
}
```

## 5. 导入任务 Mock

```json
{
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
```

导入错误：

```json
{
  "lineNo": 18,
  "fieldName": "o_totalprice",
  "rawValue": "-1",
  "reason": "金额不能为负数"
}
```

## 6. TPC-H Mock

通用结构：

```json
{
  "queryName": "TPC-H Q5 本地供应商收入分析",
  "elapsedMs": 3521,
  "rowCount": 5,
  "records": [],
  "chartData": {},
  "explainPlan": "Hash Join ..."
}
```

Q5 示例：

```json
{
  "queryName": "TPC-H Q5 本地供应商收入分析",
  "elapsedMs": 3521,
  "rowCount": 3,
  "records": [
    {
      "nationName": "CHINA",
      "revenue": 12345678.9
    },
    {
      "nationName": "JAPAN",
      "revenue": 9876543.21
    }
  ],
  "chartData": {
    "xAxis": ["CHINA", "JAPAN"],
    "series": [12345678.9, 9876543.21]
  },
  "explainPlan": "Hash Join ..."
}
```

Q12 示例：

```json
{
  "queryName": "TPC-H Q12 运送方式与订单优先级分析",
  "elapsedMs": 840,
  "rowCount": 2,
  "records": [
    {
      "shipMode": "MAIL",
      "highLineCount": 6202,
      "lowLineCount": 9324
    }
  ],
  "chartData": {
    "xAxis": ["MAIL", "SHIP"],
    "highPrioritySeries": [6202, 5981],
    "lowPrioritySeries": [9324, 8876]
  },
  "explainPlan": "Aggregate ..."
}
```

## 7. TPC-C Mock

New-Order：

```json
{
  "transactionId": "NO-20260706-0001",
  "status": "committed",
  "orderId": 3001,
  "totalAmount": 125.5,
  "elapsedMs": 82
}
```

Payment：

```json
{
  "transactionId": "PAY-20260706-0001",
  "status": "committed",
  "customerId": 1,
  "newBalance": 520.25,
  "elapsedMs": 45
}
```

库存变动日志：

```json
{
  "logId": 1,
  "warehouseId": 1,
  "itemId": 1001,
  "oldQuantity": 100,
  "newQuantity": 95,
  "changeQuantity": -5,
  "changeReason": "new_order",
  "relatedTransactionId": "NO-20260706-0001",
  "createdAt": "2026-07-06 10:00:00"
}
```

## 8. 性能 Mock

```json
{
  "testName": "TPC-H Concurrent Query Test",
  "threadCount": 8,
  "totalRequests": 80,
  "successRequests": 80,
  "failedRequests": 0,
  "avgLatencyMs": 1260.5,
  "maxLatencyMs": 2890.2,
  "minLatencyMs": 330.1,
  "throughputQps": 6.35,
  "records": [
    {
      "threadCount": 1,
      "avgLatencyMs": 410.2,
      "throughputQps": 2.43
    },
    {
      "threadCount": 2,
      "avgLatencyMs": 590.8,
      "throughputQps": 3.38
    }
  ],
  "chartData": {
    "xAxis": [1, 2, 4, 8],
    "latencySeries": [410.2, 590.8, 910.4, 1260.5],
    "throughputSeries": [2.43, 3.38, 4.52, 6.35]
  }
}
```

