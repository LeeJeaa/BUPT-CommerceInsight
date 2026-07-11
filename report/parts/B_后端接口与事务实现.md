# B 后端接口与事务实现

## 1. 职责范围

成员 B 负责 SpringBoot 后端、DTO/VO 字段映射、统一响应、统一异常、登录鉴权、用户管理、系统演示导入导出、业务查询、TPC-H 查询接口、TPC-C New-Order/Payment 事务服务和 Mock Repository。

不负责 Docker、dbgen、COPY 正式导入、Python 压测和 ECharts 页面。

## 2. 后端结构

```text
backend/
  src/main/java/com/bupt/commerceinsight/
    common/          统一响应、分页、异常、错误码
    config/          鉴权上下文、Token 拦截器、CORS
    auth/            登录注册
    user/            用户列表、审批、禁用
    importexport/    系统演示导入导出
    query/           客户查询、订单收入查询
    tpch/            Q1/Q5/Q12/Q14 查询接口
    tpcc/            New-Order、Payment
    performance/     性能结果读取接口
```

各业务模块通过 Spring profile 保持双实现：

```text
mock：不依赖数据库，供 C 并行联调
dev：JdbcTemplate/MyBatis 连接 PostgreSQL
```

## 3. 统一响应与异常

统一成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

统一分页响应：

```json
{
  "pageNo": 1,
  "pageSize": 20,
  "total": 100,
  "records": []
}
```

统一错误码采用项目冻结值：

```text
400, 401, 403, 404, 409, 500
```

## 4. 字段映射

数据库和 A 的 SQL 输出使用 `snake_case`，后端 API 和 VO 使用 `lowerCamelCase`。

示例：

| 数据库/SQL | API |
|---|---|
| `nation_name` | `nationName` |
| `ship_mode` | `shipMode` |
| `elapsed_ms` | `elapsedMs` |
| `success_count` | `successCount` |
| `throughput` | `throughput` |

## 5. Mock Repository

当前默认 `mock` profile 不依赖 PostgreSQL，可独立支持 C 联调：

```bash
cd backend
./mvnw spring-boot:run
```

测试账号：

```text
admin / 123456
admin / admin123
user1 / user123
```

## 6. TPC-H 接口

已实现：

```text
GET /api/tpch/q1
GET /api/tpch/q5
GET /api/tpch/q12
GET /api/tpch/q14
```

每个接口返回：

```text
queryName
elapsedMs
rowCount
records
chartData
explainPlan
```

Mapper XML 使用 A 提供的 SQL SELECT 主体，`dev` profile 由 `MyBatisTpchService` 执行真实 PostgreSQL 查询并写入 `query_log`。

Q1 返回完整冻结字段，包括：

```text
sumCharge
avgPrice
avgDisc
```

## 7. TPC-C 事务

已实现：

```text
POST /api/tpcc/new-order
POST /api/tpcc/payment
```

`mock` profile 返回稳定示例数据。

`dev` profile 中 `JdbcTpccService` 使用：

```text
@Transactional(rollbackFor = Exception.class)
JdbcTemplate
```

New-Order 中设置触发器上下文：

```sql
SELECT set_config('app.transaction_id', ?, true);
SELECT set_config('app.change_type', 'new_order', true);
```

库存扣减走 `stock` 更新，触发器可写入 `stock_change_log`。

事务编号使用日期与 UUID 后缀组合，避免服务重启后重复。事务方法均配置：

```text
@Transactional(rollbackFor = Exception.class)
```

## 8. 性能接口

已实现：

```text
GET /api/performance/results?testType=tpch
GET /api/performance/results?testType=tpcc
```

对外字段按 API 契约：

```text
successCount
failCount
avgLatencyMs
throughput
chartData.latencySeries
chartData.throughputSeries
```

## 9. 验证结果

后端单元/接口测试：

```text
./mvnw test
```

当前测试覆盖：

```text
统一响应
健康检查
登录注册
用户管理
导入导出
业务查询
TPC-H Q1/Q5/Q12/Q14
TPC-C New-Order/Payment
性能结果接口
Mock/dev profile 隔离
TPC-C 事务注解规则
MyBatis TPC-H 字段映射与 query_log 写入
```

当前共 23 个测试，全部通过。

## 10. 系统演示导入

`dev` profile 支持 `orders` 和 `lineitem` 文件：

```text
逐行读取
程序侧字段校验
合法行批量写入
错误行写入 import_error_log
任务统计写入 import_task
```

该功能只用于系统验收演示，不替代 D 的 dbgen/COPY 正式数据初始化。

## 11. 真实库联调状态

1. D 启动 PostgreSQL Docker。
2. 确认 A 的 `V1 -> V2 -> V3 -> V4 -> V8 -> V9 -> V10 -> V11` 已执行。
3. 使用 `dev` profile 启动后端。
4. 重点验证 TPC-H Mapper、New-Order 触发器上下文、Payment 更新余额。
5. 如 SQL 字段变化，由 A 更新 SQL/表契约，B 只改 Mapper/DTO 映射。

本次待审环境未开放 `localhost:5432`，因此没有声称完成 PostgreSQL HTTP 实测。已完成 Mock HTTP 冒烟、Java 编译、单元/接口/结构测试；真实库联调按 `docs/docs-B/B后端真实库联调清单.md` 在 A/D 基线环境执行。
