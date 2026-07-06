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

登录成功后使用：

```text
Authorization: Bearer mock-token
```

## curl 示例

登录：

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
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

## 责任边界

本后端只负责系统演示导入接口，不负责 D 的 dbgen、COPY 正式导入和 Python 压测脚本。
