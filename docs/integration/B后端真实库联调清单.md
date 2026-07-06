# B 后端真实库联调清单

> 适用分支：`dev/b-backend-service`
> 责任边界：B 负责后端 JDBC/MyBatis 适配；D 负责 Docker、正式数据和性能环境；A 负责 SQL 与字段语义。

## 1. 前置检查

1. 当前分支为 `dev/b-backend-service`。
2. PostgreSQL 地址为 `localhost:5432/tpc_commerce`。
3. 账号为 `tpc_admin / tpc_password`。
4. 开发样例环境已按以下顺序执行：

```text
V1 -> V2 -> V3 -> V4 -> V8 -> V9 -> V10 -> V11
```

5. 不由 B 修改 `sql/`、`db/init/` 或 Docker 配置。

## 2. 启动

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

健康检查：

```bash
curl -s http://localhost:8080/api/health
```

## 3. 登录与用户

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

`dev` profile 会把 V11 的 `change_me_hash` 替换为 BCrypt，并在缺失时补充 `user1 / user123`。

检查：

```sql
SELECT user_id, username, role, status, password_hash
FROM app_user
ORDER BY user_id;
```

## 4. TPC-H

依次调用：

```text
GET /api/tpch/q1?shipDate=1998-09-01
GET /api/tpch/q5?regionName=ASIA&startDate=1994-01-01&endDate=1995-01-01
GET /api/tpch/q12?shipMode1=MAIL&shipMode2=SHIP&startDate=1994-01-01&endDate=1995-01-01
GET /api/tpch/q14?month=1995-09-01
```

核对：

```sql
SELECT query_name, query_params, elapsed_ms, row_count, executed_by, executed_at
FROM query_log
ORDER BY query_log_id DESC
LIMIT 10;
```

Q1 必须包含 `sumCharge`、`avgPrice`、`avgDisc`；Q14 必须包含 `promoRevenuePercent`。

## 5. TPC-C

调用 New-Order 后检查：

```sql
SELECT transaction_id, transaction_type, status, elapsed_ms
FROM transaction_log
ORDER BY transaction_log_id DESC
LIMIT 5;

SELECT warehouse_id, item_id, old_quantity, new_quantity,
       change_quantity, change_type, related_transaction_id
FROM stock_change_log
ORDER BY log_id DESC
LIMIT 10;
```

调用 Payment 后检查：

```sql
SELECT w_id, w_ytd FROM warehouse WHERE w_id = 1;
SELECT d_w_id, d_id, d_ytd FROM district WHERE d_w_id = 1 AND d_id = 1;
SELECT c_w_id, c_d_id, c_id, c_balance, c_ytd_payment, c_payment_cnt
FROM tpcc_customer
WHERE c_w_id = 1 AND c_d_id = 1 AND c_id = 1;
```

使用不存在商品或库存不足数量调用 New-Order，确认订单、订单行、库存和事务内日志均回滚。

## 6. 系统演示导入

仅使用 D 提供的小型 `orders` 或 `lineitem` 测试文件，不使用本接口替代正式 COPY。

```bash
curl -s -X POST http://localhost:8080/api/import/tasks \
  -H "Authorization: Bearer mock-token" \
  -F "tableName=orders" \
  -F "file=@orders_sample.tbl"
```

随后轮询：

```text
GET /api/import/tasks/{taskId}
GET /api/import/tasks/{taskId}/errors
```

数据库核对：

```sql
SELECT * FROM import_task ORDER BY task_id DESC LIMIT 5;
SELECT * FROM import_error_log ORDER BY error_id DESC LIMIT 20;
```

## 7. 性能结果

D 写入 `performance_result` 后调用：

```text
GET /api/performance/results?testType=tpch
GET /api/performance/results?testType=tpcc
```

对外字段必须为：

```text
successCount
failCount
throughput
chartData.latencySeries
chartData.throughputSeries
```

## 8. 失败处理

1. 表或字段不一致：先由 A 核对 `table-contract.md` 和 SQL 资产，B 不直接改 A 的字段设计。
2. 数据库无法启动或缺少正式数据：交给 D 处理环境与导入。
3. API 字段不一致：B 修正 DTO/VO/Mapper，不让 C 依赖数据库字段。
4. 所有联调修复仍提交到 `dev/b-backend-service`，不直接推送 `main` 或 `develop`。
