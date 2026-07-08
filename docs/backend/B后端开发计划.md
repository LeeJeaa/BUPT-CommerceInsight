# 成员 B 后端开发计划

> 适用分支：`dev/b-backend-service`  
> 适用角色：成员 B，SpringBoot 后端接口与事务服务负责人  
> 依据文档：`README.md`、`docs/00_冻结文档.md`、`docs/分工指南.md`、`docs/api/*`、`docs/database/*`、`docs/docs-A/*`、课程设计指导书 PDF  
> 当前复杂度：large  
> 核心原则：不修改 A 的 SQL 源资产，不修改冻结 API/Mock 契约；B 只在后端完成 DTO/VO 映射、Mock Repository、接口、事务和运行说明。

## 1. 目标

1. 搭建 `backend/` SpringBoot 后端工程。
2. 提供统一响应、分页响应、统一异常处理、CORS 和登录鉴权。
3. 先用 Mock Repository 支持 C 并行开发。
4. 实现认证、用户管理、导入导出、业务查询、TPC-H 查询、TPC-C 事务和性能结果接口。
5. 预留 PostgreSQL JDBC/MyBatis 接入，后续可切真实数据库。
6. 为 D 提供后端启动说明、测试账号、可重复调用接口和 curl 示例。

## 2. 非目标

1. 不维护 Docker、dbgen、COPY 正式导入和 Python 压测。
2. 不修改 `sql/` 中 A 的业务 SQL。
3. 不修改 `db/init/` 初始化入口。
4. 不主写 Vue 页面、ECharts 图表或前端 Mock JSON。
5. 不擅自修改冻结契约文档：`docs/api/api-contract.md`、`docs/api/mock-contract.md`、`docs/database/*`、`docs/git/git协作指南.md`。

## 3. 关键字段映射

| 来源 | 数据库/内部字段 | 对外 API 字段 |
|---|---|---|
| Q14 | `promo_revenue_percent` | `promoRevenuePercent` |
| 导入错误 | `line_number/field_value/error_reason` | `lineNumber/fieldValue/errorReason` |
| 性能结果 | `success_count/fail_count/throughput` | `successCount/failCount/throughput` |
| Payment 请求 | A 的部分联调示例使用 `amount` | 主字段为 `paymentAmount`，兼容读取 `amount` |
| Q1 | A SQL 的 snake_case 输出别名 | `sumCharge/avgPrice/avgDisc` 等 lowerCamelCase 字段 |

## 4. 技术方案

1. SpringBoot 3 + Java 17。
2. MyBatis + PostgreSQL JDBC 作为真实库接入路径。
3. 显式 `mock` profile 不依赖数据库，便于 C 独立演示。
4. 真实联调必须显式使用 `dev` 或 `prod` profile，并连接 PostgreSQL 16：`jdbc:postgresql://localhost:5432/tpc_commerce`。
5. 统一响应类型：
   - `ApiResponse<T>`
   - `PageResponse<T>`
6. 权限模型：
   - `/api/auth/register`、`/api/auth/login` 放行。
   - 其他接口要求 `Authorization: Bearer <token>`。
   - 管理员接口要求 `role=admin`。
7. Mock token：
   - 管理员：`mock-token-admin`
   - 普通用户：`mock-token-user`
   - 兼容契约示例：`mock-token`

## 5. 阶段 TODO

### 阶段 0：设计与文档

- [x] 阅读所有 docs 和指导书 PDF。
- [x] 切换到 `dev/b-backend-service`。
- [x] 创建 B 后端开发计划。
- [x] 自查是否误改冻结契约。
- [x] 本地提交：`docs(backend): add member B backend plan`

### 阶段 1：后端骨架、统一响应与异常

- [x] 创建 `backend/` SpringBoot 工程。
- [x] 配置 Maven、Java 17、Spring Web、Validation、MyBatis、PostgreSQL JDBC。
- [x] 实现 `ApiResponse`、`PageResponse`、`ErrorCode`、`BusinessException`、`GlobalExceptionHandler`。
- [x] 实现基础鉴权拦截器和 CORS。
- [x] 增加健康检查接口 `/api/health` 作为后端自测入口。
- [x] 运行 `mvn test`。
- [x] 自查：只新增 B 目录和 B 文档，不改 A/C/D 目录。
- [x] 本地提交：`feat(backend): add spring boot base project`

### 阶段 2：认证、用户、导入导出、业务查询

- [x] 实现 `POST /api/auth/register`。
- [x] 实现 `POST /api/auth/login`。
- [x] 实现 `GET /api/users`。
- [x] 实现 `PUT /api/users/{userId}/approve`。
- [x] 实现 `PUT /api/users/{userId}/disable`。
- [x] 实现 `POST /api/import/tasks` Mock。
- [x] 实现 `GET /api/import/tasks/{taskId}` Mock。
- [x] 实现 `GET /api/import/tasks/{taskId}/errors` Mock。
- [x] 实现 `GET /api/export/table/{tableName}` CSV 文件流 Mock。
- [x] 实现 `GET /api/query/customers` Mock。
- [x] 实现 `GET /api/query/order-revenue` Mock。
- [x] 运行接口级测试。
- [x] 自查字段全部 lowerCamelCase，导入错误字段按 `api-contract.md`。
- [x] 本地提交：`feat(backend): add auth user import and query mock apis`

### 阶段 3：TPC-H Q1/Q5/Q12/Q14

- [x] 实现 TPC-H 通用 `TpchResultVO`。
- [x] 实现 `GET /api/tpch/q5`。
- [x] 实现 `GET /api/tpch/q12`。
- [x] 实现 `GET /api/tpch/q1`。
- [x] 实现 `GET /api/tpch/q14`。
- [x] 构造 `chartData`。
- [x] 预留 MyBatis Mapper SQL，复制 A 后端适配包 SELECT 主体。
- [x] 运行接口级测试。
- [x] 自查字段与 `api-contract.md`、`mock-contract.md` 对齐。
- [x] 本地提交：`feat(tpch): add q1 q5 q12 q14 mock apis`

### 阶段 4：TPC-C 事务和性能接口

- [x] 实现 `POST /api/tpcc/new-order` Mock。
- [x] 实现 `POST /api/tpcc/payment` Mock。
- [x] 预留真实事务 Service，使用 `@Transactional(rollbackFor = Exception.class)`。
- [x] 在真实事务骨架中设置 `app.transaction_id` 和 `app.change_type`。
- [x] 实现 `GET /api/performance/results` Mock。
- [x] 运行接口级测试。
- [x] 自查 New-Order 库存变化路径可写 `stock_change_log`，不承担 Python 压测。
- [x] 本地提交：`feat(tpcc): add transaction and performance mock apis`

### 阶段 5：最终自查与待审版

- [x] 运行 `mvn test`。
- [x] 启动后端并执行 curl 冒烟测试。
- [x] 检查 Git diff 未修改 A/C/D 责任文件。
- [x] 检查 API 字段没有 `snake_case` 外泄。
- [x] 检查分支为 `dev/b-backend-service`。
- [x] 更新 `backend/README.md`，给 C/D 测试账号、启动命令和接口示例。
- [x] 更新 `report/parts/B_后端接口与事务实现.md`。
- [x] 本地提交：`fix(backend): align contracts and add review handoff`

### 阶段 6：真实 PostgreSQL 通用服务适配

- [x] 抽取 `UserRepository`，保留 Mock 实现并新增 JDBC 实现。
- [x] 注册密码使用 BCrypt，`dev` profile 自动修复 V11 占位哈希并补充测试账号。
- [x] Token Store 支持已登录用户动态 token。
- [x] 导入导出拆分 Mock/JDBC 实现。
- [x] `orders`、`lineitem` 系统演示导入支持校验、批量写入、错误日志和任务状态。
- [x] 业务查询拆分 Mock/JDBC 实现。
- [x] 性能结果拆分 Mock/JDBC 实现。
- [x] 自查未修改 A 的 SQL、表结构或冻结契约。
- [x] 本地提交：`feat(backend): add postgres service adapters`

### 阶段 7：TPC-H 真实查询与规则补全

- [x] TPC-H Service 拆分 Mock/MyBatis 实现。
- [x] `dev` profile 执行 Q1/Q5/Q12/Q14 后写 `query_log`。
- [x] Q1 补齐 `sumCharge/avgPrice/avgDisc` 冻结字段。
- [x] TPC-C 事务编号改为 UUID 后缀，避免服务重启后唯一键冲突。
- [x] 增加 profile 与事务注解结构测试。
- [x] 增加 MyBatis 服务字段映射测试。
- [x] 运行 22 个单元/接口/结构测试。
- [x] 本地提交：`feat(tpch): connect postgres query services`

### 阶段 8：真实库交付说明与最终待审版

- [x] 导入导出接口补充管理员权限。
- [x] 导出白名单排除 `app_user`。
- [x] 业务查询非法日期统一返回 400。
- [x] 更新后端运行说明、报告章节和真实库联调清单。
- [x] 记录本地 PostgreSQL 端口不可用，不伪造 dev profile 联调结果。
- [x] 运行最终测试和 Mock HTTP 冒烟。
- [x] 检查提交作者、分支和责任目录。
- [x] 本地提交：`docs(backend): finalize postgres handoff`

## 6. 验证计划

1. 编译测试：`cd backend && ./mvnw test` 或 `mvn test`。
2. Mock 启动测试：`cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=mock`。
3. PostgreSQL 启动测试：A/D 基线环境可用后运行 `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`。
4. 冒烟接口：
   - `POST /api/auth/login`
   - `GET /api/users`
   - `GET /api/tpch/q5`
   - `POST /api/tpcc/new-order`
   - `GET /api/performance/results`
5. Git 自查：
   - 当前分支必须是 `dev/b-backend-service`。
   - 提交作者必须是 `saymyzj <2695364042@qq.com>`。
   - 不推送远程，等待用户审核。

## 7. 每阶段自查模板

```text
阶段：
分支：
提交：
验证：
字段契约：
是否修改 A/C/D 责任目录：
是否修改冻结契约：
遗留风险：
```
