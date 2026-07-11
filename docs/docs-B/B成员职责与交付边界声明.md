# 成员 B 职责与交付边界声明

声明人：成员 B

适用分支：`dev/b-backend-service`

更新日期：2026-07-11

## 1. 负责范围

成员 B 负责 `backend/`，主要包括：

- Spring Boot 后端工程、统一响应、全局异常处理和参数校验。
- 登录、注册、token 鉴权、角色权限和用户管理。
- `orders`、`lineitem` 系统演示导入、错误日志和白名单导出。
- Dashboard、客户、订单收入、零部件供应等业务查询。
- TPC-H Q1/Q5/Q12/Q14 接口及查询日志。
- TPC-C New-Order、Payment 事务接口及事务日志。
- 性能结果写入、读取接口和对应 DTO。
- 后端 mock/dev/prod profile、接口测试和真实库集成测试入口。

## 2. 不负责范围

- A 负责的数据库模型、SQL 基线和字段语义设计。
- C 负责的 Vue 页面、图表和前端交互实现。
- D 负责的 Docker、dbgen、正式 COPY、Python 压测脚本和性能报告生成。
- 未经团队决定扩展的导入表；当前不支持 `partsupp` 导入。

## 3. Profile 与 token 声明

- `mock`：仅用于前端独立演示，可使用预置的 `mock-token`、`mock-token-admin`、`mock-token-user`。
- `dev`、`prod`：连接真实 PostgreSQL，不预置任何 token；登录成功后签发随机 UUID token。
- 真实联调文档和脚本必须使用登录响应中的 `data.token`，不得把 `mock-token` 当成 dev/prod 管理员凭证。

## 4. 导入范围声明

后端系统演示导入仅支持 `orders`、`lineitem`。合法行继续导入，非法行写入 `import_error_log`。主键冲突、外键缺失和数据库写入错误必须产生可读明细。

正式数据装载、dbgen 和 COPY 不属于本接口职责。

## 5. 性能结果声明

成员 B 提供管理员 `POST /api/performance/results` 和 `GET /api/performance/results?testType=tpch|tpcc`。成员 D 的压测脚本负责产生指标，并调用 POST 或通过约定流程写入；成员 B 不负责生成压测负载和性能报告文件。

## 6. 验证口径

- 默认回归：`cd backend && ./mvnw test`
- 真实库集成：`RUN_DEV_INTEGRATION_TESTS=true ./mvnw -Dtest=DevPostgresIntegrationTest test`
- 若 PostgreSQL 或 Docker 环境不可用，必须明确记录跳过原因，不得声称真实库验收已通过。

详细修复记录见 `B后端审查修复说明-2026-07-11.md`，真实库步骤见 `B后端真实库联调清单.md`。
