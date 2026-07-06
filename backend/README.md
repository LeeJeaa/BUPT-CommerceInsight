# TPC CommerceInsight Backend

成员 B 后端服务。当前默认使用 `mock` profile，不依赖 PostgreSQL，可直接支持成员 C 前后端联调和成员 D 编写 HTTP 压测脚本。

## 启动

```bash
cd backend
./mvnw spring-boot:run
```

默认端口：

```text
http://localhost:8080
```

默认 profile：

```text
mock
```

切换真实 PostgreSQL 开发库：

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

`dev` profile 使用仓库冻结的 PostgreSQL 16 连接：

```text
jdbc:postgresql://localhost:5432/tpc_commerce
tpc_admin / tpc_password
```

`dev` profile 启动前，数据库必须按 A/D 文档完成初始化。开发样例环境执行顺序：

```text
V1 -> V2 -> V3 -> V4 -> V8 -> V9 -> V10 -> V11
```

真实库适配范围：

```text
认证与用户：JdbcUserRepository + BCrypt
系统演示导入：orders/lineitem 按行校验、批量写入、错误日志
业务查询：客户查询、订单收入查询
TPC-H：MyBatis Q1/Q5/Q12/Q14，并写 query_log
TPC-C：JdbcTemplate New-Order/Payment Spring 事务
性能结果：读取 performance_result
```

## 测试

```bash
cd backend
./mvnw test
```

## 测试账号

| username | password | role | status |
|---|---|---|---|
| `admin` | `admin123` 或 `123456` | `admin` | `approved` |
| `user1` | `user123` | `user` | `approved` |

`mock` profile 的管理员同时接受 `admin123` 和 `123456`；`dev` profile 使用 `admin123`。启动 `dev` profile 时，后端会把 V11 的 `change_me_hash` 升级为 BCrypt，并补充 `user1` 测试账号，不修改 A 的 SQL 文件。

登录成功后使用：

```text
Authorization: Bearer mock-token
```

## curl 示例

登录：

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

用户列表：

```bash
curl -s "http://localhost:8080/api/users?pageNo=1&pageSize=20" \
  -H "Authorization: Bearer mock-token"
```

TPC-H Q5：

```bash
curl -s "http://localhost:8080/api/tpch/q5?regionName=ASIA&startDate=1994-01-01&endDate=1995-01-01" \
  -H "Authorization: Bearer mock-token"
```

New-Order：

```bash
curl -s -X POST http://localhost:8080/api/tpcc/new-order \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer mock-token" \
  -d '{"warehouseId":1,"districtId":1,"customerId":1,"items":[{"itemId":1001,"quantity":5}]}'
```

Payment：

```bash
curl -s -X POST http://localhost:8080/api/tpcc/payment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer mock-token" \
  -d '{"warehouseId":1,"districtId":1,"customerId":1,"paymentAmount":100.00}'
```

性能结果：

```bash
curl -s "http://localhost:8080/api/performance/results?testType=tpch" \
  -H "Authorization: Bearer mock-token"
```

## 字段口径

对外 API 严格使用 `docs/api/api-contract.md` 和 `docs/api/mock-contract.md` 中的 lowerCamelCase 字段。

数据库字段和 A 的 SQL 输出别名仍为 snake_case，由后端 DTO/VO 和 Mapper 负责映射，不直接暴露给 C。

Q1 已覆盖冻结字段：

```text
sumQuantity
sumBasePrice
sumDiscountedPrice
sumCharge
avgQuantity
avgPrice
avgDisc
countOrder
```

## 系统演示导入

`dev` profile 仅接收 `orders` 和 `lineitem` 的 TPC-H 管道分隔文件或 CSV。后端完成程序侧校验、批量写入、`import_task` 状态更新和 `import_error_log` 错误记录。

导入、任务查询、错误查看和表导出仅管理员可调用。表导出使用固定白名单，不导出 `app_user` 密码摘要。

## 责任边界

本后端只负责系统演示导入接口，不负责 D 的 dbgen、COPY 正式导入和 Python 压测脚本。

真实库逐项联调步骤见 `docs/integration/B后端真实库联调清单.md`。本次待审环境未开放本机 `localhost:5432`，因此已完成代码、编译、Mock HTTP 冒烟和结构测试，PostgreSQL HTTP 联调需在 A/D 提供的基线环境执行。
