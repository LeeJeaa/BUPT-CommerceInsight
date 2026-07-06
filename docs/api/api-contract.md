# API 接口规范

## 1. 通用约定

Base URL：

```text
/api
```

统一响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

统一分页：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "pageNo": 1,
    "pageSize": 20,
    "total": 100,
    "records": []
  }
}
```

命名约定：

```text
请求和响应 JSON 字段使用 lowerCamelCase。
时间字段使用 ISO-8601 字符串或 yyyy-MM-dd HH:mm:ss，项目内保持一致。
金额使用 number，不在接口中拼接货币单位。
后端负责 snake_case 数据库字段到 lowerCamelCase API 字段的映射。
```

## 2. 错误码

| code | 含义 |
|---|---|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或 token 无效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 状态冲突或主键冲突 |
| 500 | 服务端错误 |

## 3. 认证与用户

### POST `/api/auth/register`

请求：

```json
{
  "username": "alice",
  "password": "123456",
  "realName": "Alice",
  "email": "alice@example.com"
}
```

响应 `data`：

```json
{
  "userId": 1,
  "username": "alice",
  "status": "pending"
}
```

### POST `/api/auth/login`

请求：

```json
{
  "username": "admin",
  "password": "123456"
}
```

响应 `data`：

```json
{
  "token": "mock-token",
  "userId": 1,
  "username": "admin",
  "role": "admin",
  "status": "approved"
}
```

### GET `/api/users`

查询参数：`pageNo`、`pageSize`、`status`、`keyword`。

`records` 字段：

```json
{
  "userId": 1,
  "username": "alice",
  "realName": "Alice",
  "email": "alice@example.com",
  "role": "user",
  "status": "pending",
  "createdAt": "2026-07-06 10:00:00"
}
```

### PUT `/api/users/{userId}/approve`

响应 `data`：

```json
{
  "userId": 1,
  "status": "approved"
}
```

### PUT `/api/users/{userId}/disable`

响应 `data`：

```json
{
  "userId": 1,
  "status": "disabled"
}
```

## 4. 数据导入导出

### POST `/api/import/tasks`

表单参数：

```text
tableName
file
```

响应 `data`：

```json
{
  "taskId": 1001,
  "tableName": "orders",
  "fileName": "orders_sample.txt",
  "status": "running",
  "totalRows": 0,
  "successRows": 0,
  "failedRows": 0
}
```

### GET `/api/import/tasks/{taskId}`

响应 `data`：

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

### GET `/api/import/tasks/{taskId}/errors`

分页 `records` 字段：

```json
{
  "lineNo": 18,
  "fieldName": "o_totalprice",
  "rawValue": "-1",
  "reason": "金额不能为负数"
}
```

### GET `/api/export/table/{tableName}`

返回 CSV/Excel 文件流。

## 5. 业务查询

### GET `/api/query/customers`

查询参数：`keyword`、`nationName`、`pageNo`、`pageSize`。

`records` 字段：

```json
{
  "customerKey": 1,
  "customerName": "Customer#000000001",
  "nationName": "CHINA",
  "accountBalance": 711.56,
  "marketSegment": "BUILDING"
}
```

### GET `/api/query/order-revenue`

查询参数：`startDate`、`endDate`、`pageNo`、`pageSize`。

`records` 字段：

```json
{
  "orderKey": 1,
  "orderDate": "1996-01-02",
  "customerName": "Customer#000000001",
  "revenue": 172799.49
}
```

## 6. TPC-H 分析

通用响应 `data`：

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

### GET `/api/tpch/q1`

参数：`shipDate`。

`records` 字段：

```json
{
  "returnFlag": "A",
  "lineStatus": "F",
  "sumQuantity": 37734107,
  "sumBasePrice": 56586554400.73,
  "sumDiscountedPrice": 53758257134.87,
  "avgQuantity": 25.52,
  "countOrder": 1478493
}
```

### GET `/api/tpch/q5`

参数：`regionName`、`startDate`、`endDate`。

`records` 字段：

```json
{
  "nationName": "CHINA",
  "revenue": 12345678.9
}
```

### GET `/api/tpch/q12`

参数：`shipMode1`、`shipMode2`、`startDate`、`endDate`。

`records` 字段：

```json
{
  "shipMode": "MAIL",
  "highLineCount": 6202,
  "lowLineCount": 9324
}
```

### GET `/api/tpch/q14`

参数：`month`。

`records` 字段：

```json
{
  "promoRevenue": 16.38
}
```

## 7. TPC-C 事务

### POST `/api/tpcc/new-order`

请求：

```json
{
  "warehouseId": 1,
  "districtId": 1,
  "customerId": 1,
  "items": [
    {
      "itemId": 1001,
      "quantity": 5
    }
  ]
}
```

响应 `data`：

```json
{
  "transactionId": "NO-20260706-0001",
  "status": "committed",
  "orderId": 3001,
  "totalAmount": 125.5,
  "elapsedMs": 82
}
```

### POST `/api/tpcc/payment`

请求：

```json
{
  "warehouseId": 1,
  "districtId": 1,
  "customerId": 1,
  "paymentAmount": 100.0
}
```

响应 `data`：

```json
{
  "transactionId": "PAY-20260706-0001",
  "status": "committed",
  "customerId": 1,
  "newBalance": 520.25,
  "elapsedMs": 45
}
```

## 8. 性能结果

### GET `/api/performance/results`

查询参数：`testType`。

响应 `data`：

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
  "records": [],
  "chartData": {
    "xAxis": [1, 2, 4, 8],
    "latencySeries": [410.2, 590.8, 910.4, 1260.5],
    "throughputSeries": [2.43, 3.38, 4.52, 6.35]
  }
}
```

