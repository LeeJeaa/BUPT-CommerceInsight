# Backend Review Fixes (2026-07-11)

## Summary

修复成员 B 后端审查中发现的真实 profile 固定 token、导入状态与逐行错误、性能结果无法入库、非法 JSON 返回 500 等问题，并补充 mock 与真实 PostgreSQL 场景的自动化验证。

## Complexity

`large`。改动跨认证、全局异常、导入状态机与批处理、性能 API、接口文档及真实数据库集成测试，且涉及错误恢复和权限边界。

## Context

- 当前分支 `dev/b-backend-service` 已于 2026-07-11 快进合并最新 `origin/develop`（`e1685f7`）。
- `AuthTokenStore` 在所有 profile 预置 mock token，且 dev/prod 登录管理员时签发固定 token。
- JDBC 导入返回处理前缓存的 `running` 对象；冲突行被 `ON CONFLICT DO NOTHING` 静默跳过。
- JDBC 导入只做部分格式校验，未完整校验外键、业务范围及数据库异常明细。
- 导入功能统一只支持 `orders`、`lineitem`；本次不增加 `partsupp` 导入。
- 性能服务只有查询，没有管理员写入接口。
- 全局异常处理未捕获 `HttpMessageNotReadableException`。
- 工作树已有与本任务无关的 `db/init/00_run_sql_assets.sh` 修改和两份未跟踪 Word 文档，必须保留且不纳入提交。

## Goals

- mock token 仅在 `mock` profile 可用，dev/prod 登录始终签发随机 token。
- 导入接口返回最终任务状态，逐行记录所有非法行、主键冲突和可读数据库错误，合法行继续导入。
- 明确导入仅支持 `orders` 与 `lineitem`。
- 提供管理员性能结果写入 API，写入后可立即按 `tpch`/`tpcc` 查询。
- 非法 JSON 和字段类型错误稳定返回 HTTP 400。
- 增加真实 PostgreSQL 集成测试覆盖认证、导入与性能写入/读取。

## Non-Goals

- 不实现 `partsupp` 导入。
- 不修改前端页面或性能压测脚本的文件输出逻辑。
- 不重构为持久化 token/会话系统。

## Risks And Constraints

- 导入批次可能因单行数据库异常整体失败；实现需降级逐行重试并保留错误日志。
- 真实库测试由环境变量显式启用，默认单元测试不能依赖本地 PostgreSQL。
- 不能覆盖或提交用户已有的无关工作树改动。

## Proposed Approach

1. 将 token 存储改为默认空且统一随机签发，在 `mock` profile 独立注册固定 mock token。
2. 导入解析记录源行号，先做字段/范围/日期校验，再做数据库外键与主键校验；批量写入遇到冲突或数据库错误时形成逐行日志，并最终重新查询任务。
3. 新增带 Bean Validation 的性能结果写入 DTO，在 service 中校验计数关系及类型，管理员控制器写入数据库并返回新增记录。
4. 显式处理消息体不可读异常，并补充控制器测试。
5. 更新 API/README 导入范围和性能写入合同，扩展条件启用的 dev PostgreSQL 集成测试。

## Validation Plan

- `cd backend && mvn test`
- `cd backend && RUN_DEV_INTEGRATION_TESTS=true mvn -Dtest=DevPostgresIntegrationTest test`（本机 PostgreSQL 可用时）
- 检查 `git diff --check`，并复核只包含本任务文件。

## TODO List

- [x] Issue 1: 隔离 mock token，并覆盖未登录、普通用户、管理员权限场景
  Scope: `config/AuthTokenStore`, mock profile 初始化、认证/用户测试、dev 集成测试
  Validation: mock 测试 + 条件启用的 dev 权限验收

- [x] Issue 2: 修复导入最终状态和逐行校验/错误日志
  Scope: `importexport/JdbcImportExportService`, 导入测试、真实库集成测试
  Validation: 最终统计字段、外键/主键冲突、非法数据和数据库异常日志测试

- [x] Issue 3: 新增管理员性能结果写入接口
  Scope: performance controller/service/DTO、mock/JDBC 实现、测试
  Validation: POST 后 GET 立即读取，普通用户 403，非法字段 400

- [x] Issue 4: 非法 JSON 统一返回 400
  Scope: `GlobalExceptionHandler` 与控制器测试
  Validation: 截断 JSON、字段类型错误均断言 HTTP 400

- [x] Issue 5: 更新导入范围和性能 API 文档
  Scope: backend README、API contract / performance endpoint docs
  Validation: 文档字段、权限和 `orders/lineitem` 范围与代码一致

- [x] Issue 6: 全量回归、代码复核并创建聚焦提交
  Scope: 本任务全部改动
  Validation: Maven tests、可用时真实库集成测试、`git diff --check`

## Notes During Implementation

- 2026-07-11：远程 `develop` 从 `6228364` 更新到 `e1685f7`，个人分支从 `667c30e` 无冲突快进。
- 2026-07-11：选择仅支持 `orders`、`lineitem`，与现有控制器提示及团队当前实现保持一致。
- 2026-07-11：固定 token 改由 `mock` profile 初始化；所有登录（包括 mock）均签发 UUID，mock 固定 token 仍供 mock 接口测试使用。
- 2026-07-11：导入写入保留 JDBC batch；若 batch 抛出数据库异常则逐行重试并为失败行写入 `import_error_log`。
- 2026-07-11：`./mvnw test` 通过，共 39 个测试、0 失败；其中 4 个 dev PostgreSQL 集成测试因 `RUN_DEV_INTEGRATION_TESTS` 未启用而跳过。
- 2026-07-11：本机 `localhost:5432` 未监听且 Docker daemon 未运行，无法在本机执行真实库测试；测试已覆盖 dev 认证、orders/lineitem 导入和性能写入读取，待数据库可用时执行 `RUN_DEV_INTEGRATION_TESTS=true ./mvnw -Dtest=DevPostgresIntegrationTest test`。
- 2026-07-11：复核通过 `git diff --check`；用户原有数据库脚本 file-mode 修改与两份 Word 文档未纳入本任务。
