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

