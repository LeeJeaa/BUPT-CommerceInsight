# 成员 B 后端最终交付报告

交付人：成员 B  
Git 标识：saymyzj <2695364042@qq.com>  
固定分支：dev/b-backend-service  
交付日期：2026-07-07  
交付状态：待组内审核

## 1. 执行依据

本次后端交付严格以当前仓库文档为边界，不另起后端设计。主要依据如下：

- README.md
- docs/00_冻结文档.md
- docs/分工指南.md
- docs/api/api-contract.md
- docs/api/mock-contract.md
- docs/database/database-design.md
- docs/database/table-contract.md
- docs/database/tpch-query-contract.md
- docs/database/tpcc-transaction-design.md
- docs/integration/frontend-backend-checklist.md
- docs/git/git协作指南.md
- docs/docs-A/ 给 B/C/D 的数据库、联调、性能测试交付说明
- docs/指导书-TPC-Benchmark电商数据管理系统-2026(1)(1).pdf

## 2. 成员 B 负责范围

已完成并纳入本分支的 B 范围：

- Spring Boot 后端基础工程，Java 17，Spring Boot 3.3.7。
- 统一 API 响应、统一异常处理、参数校验错误返回。
- 登录、注册、用户信息、用户状态和管理员用户列表。
- 简单 token 鉴权、角色校验、CORS 支持。
- Mock Repository，默认 `mock` profile 可不依赖数据库运行。
- `dev` profile 下 PostgreSQL JDBC/MyBatis 接入。
- 数据导入、导出接口，包括导入错误明细和导出表白名单。
- 业务查询接口：客户查询、订单收入查询。
- TPC-H Q1/Q5/Q12/Q14 查询接口。
- TPC-C New-Order 和 Payment 事务接口。
- 性能测试结果查询接口，供 D 重复拉取测试结果。
- B 后端运行说明、真实库联调清单、报告正文片段。

B 明确不负责：

- Docker 编排、dbgen、Python 压测脚本主开发。
- A 的数据库字段设计和 SQL 基线设计修改。
- C 的前端页面和 ECharts 图表实现。
- D 合并到远程后的 `db/`、`test/`、`report/performance/` 文件修复。
- 非 B 范围的远程合并冲突处理。

## 3. 后端目录结构

当前后端代码位于 `backend/`，按业务模块拆分：

- `common/`：`ApiResponse`、`PageResponse`、`BusinessException`、`ErrorCode`、`GlobalExceptionHandler`。
- `config/`：鉴权上下文、token 存储、拦截器、Web 配置。
- `auth/`：登录注册 Controller、Service、DTO、VO。
- `user/`：用户 Repository 抽象、Mock/JDBC 实现、用户接口。
- `importexport/`：导入导出 Controller、Service、Mock/JDBC 实现、导入结果 VO。
- `query/`：业务查询 Controller、Service、Mock/JDBC 实现、查询 VO。
- `tpch/`：TPC-H Controller、Service、Mock/MyBatis 实现、Mapper、Q1/Q5/Q12/Q14 VO。
- `tpcc/`：TPC-C Controller、Service、Mock/JDBC 实现、New-Order/Payment DTO 与 VO。
- `performance/`：性能测试结果 Controller、Service、Mock/JDBC 实现、结果 VO。
- `health/`：健康检查。

## 4. 统一响应、异常和权限控制

统一响应：

- 所有接口统一返回 `ApiResponse<T>`。
- 成功响应使用 `code=0`、`message=success`、`data=...`。
- 分页和列表使用文档约定的 VO 字段，不把数据库字段直接暴露给 C。

统一异常：

- 业务异常使用 `BusinessException` 携带 `ErrorCode`。
- 全局异常处理集中在 `GlobalExceptionHandler`。
- 参数校验错误统一转成前端可读错误消息。
- 未预期异常统一返回后端错误码，避免直接暴露堆栈。

权限控制：

- `/api/auth/login`、`/api/auth/register`、`/api/health` 允许匿名访问。
- 其他接口通过 `Authorization: Bearer <token>` 校验。
- 管理员接口要求 `role=ADMIN`。
- 导入接口为管理员接口；普通用户导入会返回 403。

## 5. Mock Repository 对 C 的支持

默认配置为：

- `spring.profiles.active=mock`
- `application-mock.yml` 排除 DataSource 和 MyBatis 自动配置。

因此 C 可在无 PostgreSQL、无 Docker 的情况下启动后端联调。Mock 已覆盖：

- 登录注册与用户状态。
- 数据导入导出。
- 客户查询和订单收入查询。
- TPC-H Q1/Q5/Q12/Q14。
- TPC-C New-Order 和 Payment。
- 性能测试结果查询。

测试账号：

- 管理员：`admin / admin123`
- 普通用户：`user / user123`

该设计保证 C 只依赖 API JSON 字段，不依赖数据库字段。

## 6. API 字段对齐情况

字段对齐原则：

- API JSON 使用 lowerCamelCase。
- 数据库字段、SQL alias 可使用 snake_case，但只在 Mapper/JDBC 内部出现。
- Controller/VO 返回字段必须以 `docs/api/api-contract.md` 和 `docs/api/mock-contract.md` 为准。

已重点核对字段：

- TPC-H Q1：`returnFlag`、`lineStatus`、`sumQuantity`、`sumBasePrice`、`sumDiscountedPrice`、`sumCharge`、`avgQuantity`、`avgPrice`、`avgDiscount`、`countOrder`。
- TPC-H Q5：`nationName`、`revenue`。
- TPC-H Q12：`shipMode`、`highLineCount`、`lowLineCount`。
- TPC-H Q14：`promoRevenuePercent`。
- 导入错误：`lineNumber`、`fieldName`、`fieldValue`、`errorReason`。
- 性能结果：`successCount`、`failCount`、`throughput`。

自查结论：B 后端对外 VO 未使用 `nation_name`、`ship_mode`、`lineNo`、`rawValue`、`successRequests`、`failedRequests`、`throughputQps` 等非契约字段。

## 7. snake_case 到 lowerCamelCase 映射

实现方式：

- MyBatis 配置 `map-underscore-to-camel-case=true`。
- TPC-H SQL 中的 snake_case alias 只作为 Mapper 内部映射来源。
- JDBC 查询通过 `ResultSet` 显式读取 snake_case alias，再构造 lowerCamelCase VO。
- Controller 只返回 VO，不返回 `Map<String, Object>` 直透数据库字段。

典型映射：

- `nation_name` -> `nationName`
- `ship_mode` -> `shipMode`
- `promo_revenue_percent` -> `promoRevenuePercent`
- `line_number` -> `lineNumber`
- `success_count` -> `successCount`
- `fail_count` -> `failCount`

## 8. TPC-H 实现顺序与当前状态

已按低风险到高复用顺序完成：

1. Q1：实现分组聚合全字段，优先保证字段完整性和 C 图表基础数据。
2. Q5：实现区域、时间范围和收入聚合。
3. Q12：实现运输方式优先级统计。
4. Q14：实现促销收入占比，字段名为 `promoRevenuePercent`。

当前状态：

- Mock 实现已覆盖四个查询。
- MyBatis/PostgreSQL 实现已覆盖四个查询。
- `dev` profile 下查询会写入 `query_log`，用于后续审计与性能辅助分析。

## 9. TPC-C 事务实现顺序与当前状态

已按事务风险顺序完成：

1. 建立 New-Order 和 Payment DTO/VO，先稳定 API 字段。
2. 完成 Mock Service，支持 C 和 D 在无数据库时联调。
3. 完成 JDBC Service，使用 Spring 事务。
4. New-Order 在事务内校验仓库、客户、商品和库存，写入订单、订单行，并更新库存。
5. Payment 在事务内更新客户余额、支付次数和仓库/地区 YTD。
6. 事务失败抛出异常，由 Spring 回滚。

事务要求：

- `JdbcTpccService#createNewOrder` 使用 `@Transactional(rollbackFor = Exception.class)`。
- `JdbcTpccService#payment` 使用 `@Transactional(rollbackFor = Exception.class)`。
- New-Order 设置事务上下文，配合 A 的触发器/日志设计写入 `stock_change_log`。
- 成功返回 `committed` 状态；失败由统一异常处理返回错误响应。

## 10. 与 A 对接情况

已遵守 A 的数据库边界：

- 不修改 A 的数据库字段设计。
- JDBC/MyBatis 按 A 文档中的 PostgreSQL 连接口径默认连接 `tpc_commerce`、`tpc_admin`。
- 导入实现遵守 `orders`、`lineitem` 的字段和清洗口径。
- 导出表使用白名单，避免导出用户敏感表。
- TPC-H SQL 使用 A 的 TPC-H 表字段。
- TPC-C 事务依赖 A 的表、触发器和 `stock_change_log` 语义。

仍需 A/集成环境最终确认：

- 在完整 PostgreSQL 基线环境中执行 `docs/integration/B后端真实库联调清单.md`。
- 确认 `stock_change_log` 触发器在真实库中按 New-Order 库存变更写入。
- 确认 `query_log`、`import_task`、`import_error_log`、`performance_result` 表均已执行到位。

## 11. 与 C 联调说明

C 可优先使用 Mock 后端：

```bash
cd backend
./mvnw spring-boot:run
```

默认地址：

- `GET /api/health`
- `POST /api/auth/login`
- `GET /api/tpch/q1`
- `GET /api/tpch/q5`
- `GET /api/tpch/q12`
- `GET /api/tpch/q14`
- `POST /api/tpcc/new-order`
- `POST /api/tpcc/payment`
- `GET /api/performance/results`

联调注意：

- 登录后使用 `Authorization: Bearer <token>`。
- C 只读取 lowerCamelCase JSON 字段。
- 不应依赖数据库字段、SQL alias 或 D 的性能 mock CSV 字段。
- API 字段以 `docs/api/api-contract.md` 和 `docs/api/mock-contract.md` 为准。

## 12. 给 D 的性能测试接口

已提供：

- `GET /api/performance/results`
- `GET /api/performance/results/{id}`

返回字段遵循契约：

- `endpoint`
- `method`
- `concurrentUsers`
- `totalRequests`
- `successCount`
- `failCount`
- `avgLatencyMs`
- `p95LatencyMs`
- `throughput`
- `createdAt`

D 可重复调用接口获取后端保存的性能结果。B 不主写 Python 压测脚本，但保证接口字段稳定。

## 13. 本地自查结果

已完成的自查：

- Git 分支：`dev/b-backend-service`。
- Git 标识：`saymyzj <2695364042@qq.com>`。
- B 分支未修改 A 的数据库设计文件。
- B 分支未修改 C 前端页面。
- B 分支未修改 D 的 Python 压测脚本和性能报告。
- `backend` 单元测试通过：23 tests，0 failures，0 errors。
- 字段扫描未发现 B 对外 API 泄露非契约字段。
- `.gitignore` 的 B 侧规则包含后端 Maven 缓存、`target/`、环境文件和系统临时文件。

未完成但已明确说明的事项：

- 当前本机没有确认可用的 PostgreSQL 基线库，因此真实库 HTTP 联调需在 A/D 提供的基线环境中按清单执行。
- `origin/develop` 已包含 D 的远程交付；B 已按组长要求解决阻塞 PR 的 `.gitignore` 冲突，D 范围字段和文档风险详见 `docs/backend/B关于D远程交付问题声明.md`。

## 14. 提交与审核建议

建议审核顺序：

1. 先审核 B 后端代码是否符合 API 契约和分工边界。
2. 再按 `docs/integration/B后端真实库联调清单.md` 在真实 PostgreSQL 环境中验收。
3. 最后由组长或对应责任人处理 D 远程交付中的字段不一致和过期文档问题。

本报告对应 B 分支交付，不代表 B 已处理或认可 D 范围业务文件中的问题。
