# 前后端联调检查表

## 1. 字段检查

```text
C 页面字段来自 docs/api/mock-contract.md。
B 真实接口字段来自 docs/api/api-contract.md。
Mock 和真实接口字段一致。
没有 nation_name/timeCost 这类未冻结字段。
```

## 2. 接口检查

```text
POST /api/auth/login
POST /api/auth/register
GET /api/users
POST /api/import/tasks
GET /api/import/tasks/{taskId}
GET /api/tpch/q1
GET /api/tpch/q5
GET /api/tpch/q12
GET /api/tpch/q14
POST /api/tpcc/new-order
POST /api/tpcc/payment
GET /api/performance/results
```

## 3. 页面检查

```text
登录后 token 保存和请求头传递正常。
表格能展示 records。
图表能展示 chartData。
错误响应能展示 message。
分页参数 pageNo/pageSize 正确。
```

## 4. C 前端实现记录

```text
frontend/ 已创建 Vue 3 + Vite + JavaScript 工程。
默认 VITE_USE_MOCK 不设置时使用前端 Mock，可独立演示。
设置 VITE_USE_MOCK=false 时通过 Vite 代理请求 B 后端 /api。
登录成功后 token 保存到 Pinia/localStorage，请求拦截器自动添加 Authorization: Bearer <token>。
管理员页面包括用户管理、数据导入、数据导出；普通用户路由守卫会回到系统总览。
```

字段核查：

```text
导入错误字段使用 lineNumber / fieldName / fieldValue / errorReason。
TPC-H Q1 使用 returnFlag / lineStatus / sumQuantity / sumBasePrice / sumDiscountedPrice / sumCharge / avgQuantity / avgPrice / avgDisc / countOrder。
TPC-H Q5 使用 nationName / revenue。
TPC-H Q12 使用 shipMode / highLineCount / lowLineCount，并兼容 chartData.highSeries/lowSeries。
TPC-H Q14 使用 promoRevenuePercent。
性能页面内部统一使用 successCount / failCount / throughput / chartData.latencySeries / chartData.throughputSeries。
D 的原始 performance_mock.json 如包含 successRequests/failedRequests，会在前端适配层转换后展示。
```

待真实后端运行后复核：

```text
□ POST /api/auth/login 返回 token 后可进入系统。
□ GET /api/users 可分页显示。
□ POST /api/import/tasks 可提交 multipart 表单。
□ GET /api/import/tasks/{taskId}/errors 返回字段与页面一致。
□ GET /api/tpch/q1/q5/q12/q14 返回 records/chartData/elapsedMs。
□ POST /api/tpcc/new-order 与 POST /api/tpcc/payment 返回事务结果。
□ GET /api/performance/results 返回性能图表数据。
```

