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

## 3. 契约差异处理

| 来源 | 差异 | B 的处理 |
|---|---|---|
| `api-contract.md` vs A 展示包 | Q14 API 冻结字段为 `promoRevenue`，A SQL/展示包字段为 `promoRevenuePercent` | 对外先严格按 `api-contract.md` 返回 `promoRevenue`；真实库接入时由 SQL `promo_revenue_percent` 映射到 VO |
| `api-contract.md` vs A 展示包 | 导入错误 API 冻结为 `lineNo/rawValue/reason`，A 展示包使用 `lineNumber/fieldValue/errorReason` | 对外严格返回 `lineNo/rawValue/reason`；Repository 适配实际数据库字段 |
| `api-contract.md` vs A 展示包 | Payment 请求冻结为 `paymentAmount`，A 示例有 `amount` | DTO 主字段为 `paymentAmount`，兼容读取 `amount`，响应仍按冻结契约 |
| `mock-contract.md` vs A 展示包 | 性能冻结为 `successRequests/failedRequests/throughputQps`，A 展示包有 `successCount/failCount/throughput` | 对外严格返回 `successRequests/failedRequests/throughputQps` |
| `api-contract.md` vs A SQL | Q1 SQL 有 `sumCharge/avgPrice/avgDisc`，冻结 API Q1 示例未列这些扩展字段 | Mock/接口只保证冻结字段存在；真实映射保留扩展字段不影响 C 时再考虑 |

## 4. 技术方案

1. SpringBoot 3 + Java 17。
2. MyBatis + PostgreSQL JDBC 作为真实库接入路径。
3. 默认 `mock` profile 不依赖数据库，便于 C 联调。
4. `dev` profile 连接 PostgreSQL 16：`jdbc:postgresql://localhost:5432/tpc_commerce`。
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
- [-] 本地提交：`docs(backend): add member B backend plan`

### 阶段 1：后端骨架、统一响应与异常

- [ ] 创建 `backend/` SpringBoot 工程。
- [ ] 配置 Maven、Java 17、Spring Web、Validation、MyBatis、PostgreSQL JDBC。
- [ ] 实现 `ApiResponse`、`PageResponse`、`ErrorCode`、`BusinessException`、`GlobalExceptionHandler`。
- [ ] 实现基础鉴权拦截器和 CORS。
- [ ] 增加健康检查接口 `/api/health` 作为后端自测入口。
- [ ] 运行 `mvn test`。
- [ ] 自查：只新增 B 目录和 B 文档，不改 A/C/D 目录。
- [ ] 本地提交：`feat(backend): add spring boot base project`

### 阶段 2：认证、用户、导入导出、业务查询

- [ ] 实现 `POST /api/auth/register`。
- [ ] 实现 `POST /api/auth/login`。
- [ ] 实现 `GET /api/users`。
- [ ] 实现 `PUT /api/users/{userId}/approve`。
- [ ] 实现 `PUT /api/users/{userId}/disable`。
- [ ] 实现 `POST /api/import/tasks` Mock。
- [ ] 实现 `GET /api/import/tasks/{taskId}` Mock。
- [ ] 实现 `GET /api/import/tasks/{taskId}/errors` Mock。
- [ ] 实现 `GET /api/export/table/{tableName}` CSV 文件流 Mock。
- [ ] 实现 `GET /api/query/customers` Mock。
- [ ] 实现 `GET /api/query/order-revenue` Mock。
- [ ] 运行接口级测试。
- [ ] 自查字段全部 lowerCamelCase，导入错误字段按 `api-contract.md`。
- [ ] 本地提交：`feat(backend): add auth user import and query mock apis`

### 阶段 3：TPC-H Q1/Q5/Q12/Q14

- [ ] 实现 TPC-H 通用 `TpchResultVO`。
- [ ] 实现 `GET /api/tpch/q5`。
- [ ] 实现 `GET /api/tpch/q12`。
- [ ] 实现 `GET /api/tpch/q1`。
- [ ] 实现 `GET /api/tpch/q14`。
- [ ] 构造 `chartData`。
- [ ] 预留 MyBatis Mapper SQL，复制 A 后端适配包 SELECT 主体。
- [ ] 运行接口级测试。
- [ ] 自查字段与 `api-contract.md`、`mock-contract.md` 对齐。
- [ ] 本地提交：`feat(tpch): add q1 q5 q12 q14 mock apis`

### 阶段 4：TPC-C 事务和性能接口

- [ ] 实现 `POST /api/tpcc/new-order` Mock。
- [ ] 实现 `POST /api/tpcc/payment` Mock。
- [ ] 预留真实事务 Service，使用 `@Transactional(rollbackFor = Exception.class)`。
- [ ] 在真实事务骨架中设置 `app.transaction_id` 和 `app.change_type`。
- [ ] 实现 `GET /api/performance/results` Mock。
- [ ] 运行接口级测试。
- [ ] 自查 New-Order 库存变化路径可写 `stock_change_log`，不承担 Python 压测。
- [ ] 本地提交：`feat(tpcc): add transaction and performance mock apis`

### 阶段 5：最终自查与待审版

- [ ] 运行 `mvn test`。
- [ ] 启动后端并执行 curl 冒烟测试。
- [ ] 检查 Git diff 未修改 A/C/D 责任文件。
- [ ] 检查 API 字段没有 `snake_case` 外泄。
- [ ] 检查分支为 `dev/b-backend-service`。
- [ ] 更新 `backend/README.md`，给 C/D 测试账号、启动命令和接口示例。
- [ ] 更新 `report/parts/B_后端接口与事务实现.md`。
- [ ] 本地提交：`docs(backend): add backend review handoff`

## 6. 验证计划

1. 编译测试：`cd backend && ./mvnw test` 或 `mvn test`。
2. 启动测试：`cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=mock`。
3. 冒烟接口：
   - `POST /api/auth/login`
   - `GET /api/users`
   - `GET /api/tpch/q5`
   - `POST /api/tpcc/new-order`
   - `GET /api/performance/results`
4. Git 自查：
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
