# B 后端可压测接口说明

> 给成员 D 使用。字段仍以 `docs/api/api-contract.md` 为准，本文只补充可重复调用参数。

## 1. 登录

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

响应中读取：

```text
data.token
```

后续请求头：

```text
Authorization: Bearer <data.token>
```

以下示例中的 `$TOKEN` 均指登录响应的 `data.token`。固定 `mock-token` 仅可用于 `mock` profile，不能用于 `dev`/`prod` 压测。

## 2. TPC-H 查询端点

| 接口 | Method | 示例 |
|---|---|---|
| Q1 | GET | `/api/tpch/q1?shipDate=1998-09-01` |
| Q5 | GET | `/api/tpch/q5?regionName=ASIA&startDate=1994-01-01&endDate=1995-01-01` |
| Q12 | GET | `/api/tpch/q12?shipMode1=MAIL&shipMode2=SHIP&startDate=1994-01-01&endDate=1995-01-01` |
| Q14 | GET | `/api/tpch/q14?month=1995-09-01` |

响应关注：

```text
code
message
data.elapsedMs
data.rowCount
data.records
data.chartData
```

## 3. TPC-C 事务端点

New-Order：

```bash
curl -s -X POST http://localhost:8080/api/tpcc/new-order \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"warehouseId":1,"districtId":1,"customerId":1,"items":[{"itemId":1001,"quantity":1}]}'
```

Payment：

```bash
curl -s -X POST http://localhost:8080/api/tpcc/payment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"warehouseId":1,"districtId":1,"customerId":1,"paymentAmount":25.00}'
```

响应关注：

```text
transactionId
status
elapsedMs
```

## 4. 性能结果写入与读取

压测完成后由管理员写入：

```bash
curl -s -X POST http://localhost:8080/api/performance/results \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"testName":"TPC-H Concurrent Query Test","testType":"tpch","threadCount":8,"totalRequests":80,"successCount":80,"failCount":0,"avgLatencyMs":1260.5,"maxLatencyMs":2890.2,"minLatencyMs":330.1,"throughput":6.35}'
```

写入后可立即读取：

```bash
curl -s "http://localhost:8080/api/performance/results?testType=tpch" \
  -H "Authorization: Bearer $TOKEN"
```

响应字段使用 API 契约：

```text
successCount
failCount
avgLatencyMs
throughput
chartData.latencySeries
chartData.throughputSeries
```

注意：B 不负责 Python 压测脚本；脚本生成结果后应调用上述 POST 入库，后端保证写入和查询接口稳定、可重复调用。
