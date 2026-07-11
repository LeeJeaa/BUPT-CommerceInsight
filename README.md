# TPC CommerceInsight

> 仓库地址：[https://github.com/LeeJeaa/BUPT-CommerceInsight.git](https://github.com/LeeJeaa/BUPT-CommerceInsight.git)  
> SSH 地址：`git@github.com:LeeJeaa/BUPT-CommerceInsight.git`  
> 当前状态：数据库课程设计开发规范基线  
> 核心策略：契约先行、SQL 源目录唯一、API 字段冻结、Mock 先行、分支隔离、每日集成。

## 一、项目目标

TPC CommerceInsight 是一个面向数据库课程设计的电商数据管理与 Benchmark 分析系统。

系统围绕 TPC-H 和 TPC-C 两类 Benchmark，完成数据库建模、批量导入、业务查询、事务处理、索引优化、执行计划分析、并发性能测试和可视化展示。

系统需要实现：

1. 使用 PostgreSQL Docker 管理课程设计数据库。
2. 支持 TPC-H 数据表、TPC-C 事务表和应用辅助表设计。
3. 支持 TPC-H 数据生成、导入、行数统计和正式性能测试。
4. 支持用户注册、登录、审批、禁用等权限管理。
5. 支持系统演示导入、数据清洗、错误日志和导出。
6. 支持 TPC-H Q1/Q5/Q12/Q14 查询分析和 ECharts 可视化。
7. 支持 TPC-C New-Order 和 Payment 事务，包含提交、回滚和日志记录。
8. 支持触发器、存储过程、索引前后对比和 EXPLAIN ANALYZE。
9. 支持 Python 并发测试脚本输出平均延迟、吞吐量、成功失败数等性能指标。
10. 支持完整课程设计报告、截图、实验结果和验收演示材料。

一句话：

> 做一个“PostgreSQL + SpringBoot + Vue 3 + TPC-H/TPC-C + 性能实验 + 可视化报告”的数据库课程设计系统。

## 二、项目架构

```text
BUPT-CommerceInsight/
  backend/                         # B：SpringBoot 后端服务
  frontend/                        # C：Vue 3 前端与可视化
  sql/                             # A：唯一 SQL 源资产目录
    V1__create_tpch_tables.sql
    V2__create_tpcc_tables.sql
    V3__create_app_tables.sql
    V4__add_constraints.sql
    V5__tpch_queries.sql
    V6__tpcc_transaction_sql.sql
    V7__import_cleaning_rules.sql
    V8__triggers.sql
    V9__procedures.sql
    V10__indexes_baseline.sql
    V11__sample_data.sql
    V12__explain_baseline.sql
    demo_transaction_data.sql      # 可选：持久化 New-Order/Payment 演示事务
  db/                              # D：Docker、初始化、正式导入脚本
    docker-compose.yml
    init/00_run_sql_assets.sh
    scripts/
  docs/                            # 冻结规范与专项设计文档
  test/                            # D：性能测试脚本与结果
  report/                          # D 总控，A/B/C 提交各自章节和截图
  README.md
```

系统分层：

```text
Vue 3 + Element Plus + ECharts
        |
        | HTTP JSON API
        v
SpringBoot + MyBatis/JDBC + DTO/VO
        |
        | SQL / Transaction
        v
PostgreSQL Docker
        |
        | EXPLAIN / COPY / Benchmark
        v
Python 性能测试脚本 + 报告材料
```

关键边界：

```text
A 管数据库字段和 SQL 源资产。
B 管 API 字段映射和事务服务。
C 只依赖 API/Mock 字段，不依赖数据库字段。
D 管 Docker、正式数据、性能实验和报告整合。
```

TPC-C 口径冻结为课程最小实现：`stock` 不包含 `s_dist_01` 至 `s_dist_10`，New-Order 的 `order_line.ol_dist_info` 由 B 生成并写入；如需初始化库中出现已提交事务样例，可在 V11 后执行 `sql/demo_transaction_data.sql`。

## 三、技术栈

| 模块 | 技术 |
|---|---|
| 前端 | Vue 3、Element Plus、ECharts、Axios、Pinia、Vue Router |
| 后端 | SpringBoot、MyBatis 或 JDBC、Spring Transaction、统一异常处理 |
| 数据库 | PostgreSQL 16 Docker |
| SQL 资产 | TPC-H 表、TPC-C 表、应用表、约束、触发器、存储过程、索引 |
| 数据导入 | dbgen、COPY、系统演示导入、导入清洗、错误日志 |
| 性能测试 | Python 多线程脚本、CSV/JSON 结果 |
| 协作 | Git、Markdown 契约文档、分支开发、PR 合并 |

## 四、快速开始

### 4.1 克隆仓库

使用 SSH：

```bash
git clone git@github.com:LeeJeaa/BUPT-CommerceInsight.git
cd BUPT-CommerceInsight
```

或使用 HTTPS：

```bash
git clone https://github.com/LeeJeaa/BUPT-CommerceInsight.git
cd BUPT-CommerceInsight
```

### 4.2 首次阅读文档

所有成员开始写代码前必须先读：

1. [docs/00_冻结文档.md](docs/00_冻结文档.md)
2. [docs/分工指南.md](docs/分工指南.md)
3. [docs/README.md](docs/README.md)
4. [docs/api/api-contract.md](docs/api/api-contract.md)
5. [docs/api/mock-contract.md](docs/api/mock-contract.md)
6. [docs/database/database-design.md](docs/database/database-design.md)
7. [docs/database/sql-execution-and-import.md](docs/database/sql-execution-and-import.md)
8. [docs/git/git协作指南.md](docs/git/git协作指南.md)
9. [docs/integration/联调与验收清单.md](docs/integration/联调与验收清单.md)

### 4.3 本地联调启动

真实后端联调和 D 阶段验收必须显式使用 `dev` 或 `prod` profile，避免误跑 Mock：

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

前端切到真实接口：

```powershell
cd frontend
$env:VITE_USE_MOCK="false"
npm.cmd run dev
```

统一测试账号：

```text
admin / admin123
user1 / user123
```

### 4.4 分支组成

| 分支 | 用途 | 合并规则 |
|---|---|---|
| `main` | 最终稳定验收版，只放可演示、可提交版本 | 只能由 `develop` 验收通过后合入 |
| `develop` | 日常集成分支，四个人功能最终先合并到这里 | 每日或阶段性集成 |
| `dev/a-database-sql` | A 的数据库 SQL、表结构、触发器、存储过程、索引方案分支 | 先合入 `develop` |
| `dev/b-backend-service` | B 的 SpringBoot、MyBatis、接口、事务服务分支 | 先合入 `develop` |
| `dev/c-frontend-visual` | C 的 Vue、Element Plus、ECharts、Mock、前后端联调分支 | 先合入 `develop` |
| `dev/d-devops-performance-report` | D 的 Docker、数据导入脚本、性能测试、报告整合分支 | 先合入 `develop` |

如果远程还没有 `develop`，由负责人创建：

```bash
git checkout main
git pull origin main
git checkout -b develop
git push -u origin develop
```

四个成员创建自己的分支：

```bash
git checkout develop
git pull origin develop
git checkout -b dev/a-database-sql
git push -u origin dev/a-database-sql
```

```bash
git checkout develop
git pull origin develop
git checkout -b dev/b-backend-service
git push -u origin dev/b-backend-service
```

```bash
git checkout develop
git pull origin develop
git checkout -b dev/c-frontend-visual
git push -u origin dev/c-frontend-visual
```

```bash
git checkout develop
git pull origin develop
git checkout -b dev/d-devops-performance-report
git push -u origin dev/d-devops-performance-report
```

## 五、四人分工

| 成员 | 分支 | 主责 | 不负责 |
|---|---|---|---|
| A | `dev/a-database-sql` | TPC-H/TPC-C 表结构、应用辅助表、约束、SQL、触发器、存储过程、索引初版、清洗规则 | Docker、正式数据导入、后端接口、前端页面、Python 压测 |
| B | `dev/b-backend-service` | SpringBoot、MyBatis/JDBC、DTO/VO、登录鉴权、API、导入导出接口、TPC-H 查询接口、TPC-C 事务 | Docker、dbgen、ECharts 页面、正式压测 |
| C | `dev/c-frontend-visual` | Vue 3、Element Plus、ECharts、Mock JSON、页面、前后端联调、截图 | 数据库字段设计、SQL 优化解释、Docker、压测执行 |
| D | `dev/d-devops-performance-report` | Docker PostgreSQL、dbgen、COPY、数据库重置、行数统计、EXPLAIN、性能测试、报告整合 | 替 A 改 SQL、替 B 修接口、替 C 做页面 |

## 六、每日 Git 开发规范

### 6.1 每天开始开发前

所有成员先同步 `develop`，再回到自己的分支：

```bash
git checkout develop
git pull origin develop
git checkout <自己的分支>
git merge develop
git status
```

示例：

```bash
git checkout develop
git pull origin develop
git checkout dev/b-backend-service
git merge develop
git status
```

### 6.2 每次提交前

```bash
git status
git diff
```

确认：

```text
没有误改其他成员负责目录。
跨层字段变更已经先改 docs。
API 字段与 docs/api/api-contract.md 一致。
Mock 字段与 docs/api/mock-contract.md 一致。
数据库变更已同步 docs/database/table-contract.md 或 change-log.md。
```

提交格式：

```text
<type>(<scope>): <summary>
```

常用类型：

```text
docs, feat, fix, sql, api, ui, test, chore
```

示例：

```bash
git add .
git commit -m "sql(database): add stock change log table"
git push origin dev/a-database-sql
```

```bash
git add .
git commit -m "api(tpch): implement q5 query response"
git push origin dev/b-backend-service
```

### 6.3 每日集成到 develop

每人自测通过后再合入 `develop`。推荐通过 Pull Request 合并；如果课程团队直接命令行合并，由负责人执行：

```bash
git checkout develop
git pull origin develop
git merge dev/a-database-sql
git merge dev/b-backend-service
git merge dev/c-frontend-visual
git merge dev/d-devops-performance-report
git push origin develop
```

### 6.4 develop 合入 main

只有当 `develop` 可以完整演示、报告材料完整、联调清单通过后，才允许合入 `main`：

```bash
git checkout main
git pull origin main
git merge develop
git push origin main
```

### 6.5 绝对禁止

```text
禁止直接把未验证代码推到 main。
禁止在 db/init/ 手写第二套业务 SQL。
禁止 C 直接依赖数据库字段。
禁止 A 因前端字段名偏好直接改数据库字段。
禁止 B/C 各写一套 Mock 字段。
禁止提交大规模 dbgen 数据文件。
禁止改别人模块后不说明。
```

## 七、文档索引与改动规则

### 7.1 文档索引

| 文档 | 作用 |
|---|---|
| [docs/00_冻结文档.md](docs/00_冻结文档.md) | 全组冻结基线，跨成员变更以此为准 |
| [docs/分工指南.md](docs/分工指南.md) | 四人分工、每日计划、交付标准 |
| [docs/product/产品需求文档.md](docs/product/产品需求文档.md) | 产品目标、角色、功能和验收指标 |
| [docs/architecture/架构设计文档.md](docs/architecture/架构设计文档.md) | 系统分层、数据流、技术选型 |
| [docs/api/api-contract.md](docs/api/api-contract.md) | 后端真实接口规范 |
| [docs/api/mock-contract.md](docs/api/mock-contract.md) | 前端 Mock 和后端 Mock Repository 字段规范 |
| [docs/database/database-design.md](docs/database/database-design.md) | 数据库命名、表、事务、索引规范 |
| [docs/database/table-contract.md](docs/database/table-contract.md) | 表清单、应用辅助表和变更影响 |
| [docs/database/naming-convention.md](docs/database/naming-convention.md) | 数据库对象命名细则 |
| [docs/database/tpch-query-contract.md](docs/database/tpch-query-contract.md) | TPC-H Q1/Q5/Q12/Q14 查询契约 |
| [docs/database/tpcc-transaction-design.md](docs/database/tpcc-transaction-design.md) | TPC-C New-Order 与 Payment 事务流程 |
| [docs/database/import-cleaning-rules.md](docs/database/import-cleaning-rules.md) | 系统演示导入清洗规则 |
| [docs/database/triggers-and-procedures.md](docs/database/triggers-and-procedures.md) | 触发器和存储过程规范 |
| [docs/database/index-design.md](docs/database/index-design.md) | 索引方案与对比实验要求 |
| [docs/database/explain-baseline-analysis.md](docs/database/explain-baseline-analysis.md) | EXPLAIN 分析保存和解读规范 |
| [docs/database/sql-execution-and-import.md](docs/database/sql-execution-and-import.md) | SQL 执行顺序、开发初始化、正式导入规范 |
| [docs/database/change-log.md](docs/database/change-log.md) | 数据库变更记录 |
| [docs/git/git协作指南.md](docs/git/git协作指南.md) | 分支、提交、PR、冲突处理规范 |
| [docs/integration/联调与验收清单.md](docs/integration/联调与验收清单.md) | 联调和最终验收检查清单 |
| [docs/integration/frontend-backend-checklist.md](docs/integration/frontend-backend-checklist.md) | 前后端联调检查 |
| [docs/integration/database-deploy-checklist.md](docs/integration/database-deploy-checklist.md) | 数据库 Docker 部署检查 |

### 7.2 一定不能随意改动的文档

这些文档属于冻结契约。不能个人单独改，必须先在组内确认影响面：

```text
docs/00_冻结文档.md
docs/api/api-contract.md
docs/api/mock-contract.md
docs/database/database-design.md
docs/database/table-contract.md
docs/database/sql-execution-and-import.md
docs/database/tpch-query-contract.md
docs/database/tpcc-transaction-design.md
docs/git/git协作指南.md
```

如果确实需要修改，流程必须是：

```text
先说明变更原因
判断影响 A/B/C/D 哪些成员
先更新文档
相关成员确认
再改代码或 SQL
最后合并到 develop
```

### 7.3 可以持续补充的文档

这些文档允许对应负责人持续补充，但不能违反冻结契约：

```text
docs/database/change-log.md
docs/database/explain-baseline-analysis.md
docs/database/index-design.md
docs/database/import-cleaning-rules.md
docs/database/triggers-and-procedures.md
docs/integration/frontend-backend-checklist.md
docs/integration/database-deploy-checklist.md
report/
```

补充原则：

```text
A 可以补充数据库解释、SQL 说明、索引理由。
B 可以补充接口实现说明和后端运行说明。
C 可以补充页面截图、联调记录、演示路径。
D 可以补充 Docker、导入、EXPLAIN、性能测试和报告材料。
```

## 八、关键开发口径

所有成员必须统一以下口径：

1. `sql/` 是唯一 SQL 源资产目录。
2. `db/init/` 只是 Docker 初始化入口，不维护业务 SQL。
3. PostgreSQL 对象统一使用小写 `snake_case`，禁止双引号对象名。
4. API JSON 字段统一使用 `lowerCamelCase`。
5. B 负责数据库字段到 API 字段的 DTO/VO 映射。
6. C 只依赖 API/Mock 字段，不依赖数据库字段。
7. A 不因为前端字段名偏好直接修改数据库字段。
8. B 的 Mock Repository 和 C 的 Mock JSON 都以 [docs/api/mock-contract.md](docs/api/mock-contract.md) 为准。
9. 系统演示导入由 B/C 实现，正式 dbgen + COPY 导入由 D 实现。
10. 开发初始化模式和正式导入模式 SQL 执行顺序不同，不能混用。
11. `db/init/00_run_sql_assets.sh` 必须使用 LF 换行，并提交 `chmod +x` 后的可执行权限。
12. 第 3 天 D 只要求开始 SF=1 并确保 SF=0.1 完成，SF=0.6/SF=1 在第 4-5 天冻结。

## 九、提交材料

最终提交包建议结构：

```text
班级_姓名1_姓名2_姓名3_姓名4_课程设计报告.docx

source_code/
  backend/
  frontend/

sql/
db/
docs/
test/
report/
README.md
```

材料分工：

| 提交材料 | 主责 | 配合 |
|---|---|---|
| 数据库 SQL 与设计说明 | A | D 验证正式执行 |
| 后端源码与接口说明 | B | A 提供 SQL，C 联调 |
| 前端源码、截图和演示路径 | C | B 提供接口，D 提供性能数据 |
| Docker、正式数据、性能测试 | D | A/B/C 提供可执行材料 |
| 课程设计报告 | D 总控 | A/B/C 提交各自章节 |
| 验收演示 | 全员 | D 控流程，C 控页面，B 控接口，A 控数据库说明 |

## 十、给成员 A 的 AI Prompt

```text
我正在参与数据库课程设计项目：TPC CommerceInsight。

请严格基于当前仓库中的 README.md 和 docs/ 规范文档进行分析、开发和指导，不要脱离现有设计另起炉灶。

你必须优先阅读以下内容：

1. README.md
2. docs/00_冻结文档.md
3. docs/分工指南.md
4. docs/database/database-design.md
5. docs/database/table-contract.md
6. docs/database/naming-convention.md
7. docs/database/tpch-query-contract.md
8. docs/database/tpcc-transaction-design.md
9. docs/database/import-cleaning-rules.md
10. docs/database/triggers-and-procedures.md
11. docs/database/index-design.md
12. docs/database/sql-execution-and-import.md
13. docs/database/change-log.md

我的身份是“成员 A”，我的固定分支是：

dev/a-database-sql

我的固定技术方向是：

- TPC-H 表结构
- TPC-C 必要表结构
- 应用辅助表
- 约束、触发器、存储过程
- TPC-H Q1/Q5/Q12/Q14 SQL
- TPC-C New-Order 和 Payment SQL 流程
- 导入清洗规则
- 索引初版和样例 EXPLAIN

当前项目必须遵守以下关键口径：

- sql/ 是唯一 SQL 源资产目录
- 所有数据库对象使用小写 snake_case
- 禁止使用双引号创建表名和字段名
- 应用辅助表必须包含 stock_change_log
- A 不因为前端字段名偏好直接修改数据库字段
- C 只依赖 API 字段，字段转换由 B 的 DTO/VO 完成
- 开发初始化模式和正式导入模式 SQL 顺序不同
- D 负责正式 Docker、dbgen、COPY 和正式性能测试

我的工作边界是：

- 主写 sql/V1 到 V12
- 主写数据库设计说明、表结构契约、索引理由和 EXPLAIN 解读
- 给 B 提供稳定 SQL、字段语义和事务流程
- 给 D 提供正式导入兼容要求和索引候选方案
- 不主写 SpringBoot 后端接口
- 不主写 Vue 前端页面
- 不负责 Docker、dbgen、正式 COPY 导入和 Python 压测

请输出一份详细工作指导，必须包含：

1. 成员 A 当前负责什么，不负责什么
2. sql/ 推荐文件结构和每个 V*.sql 应写什么
3. TPC-H 和 TPC-C 表结构设计注意点
4. stock_change_log 应该如何设计
5. Q1/Q5/Q12/Q14 的 SQL 输出字段如何与 API 契约对齐
6. New-Order 和 Payment 事务 SQL 流程
7. 触发器、存储过程、索引的实现顺序
8. 如何与 B 对接 DTO/Mapper 字段
9. 如何与 D 对接正式导入和 EXPLAIN
10. Git 上每天应该怎么操作

请给出按步骤执行的清单，不要泛泛建议。
```

## 十一、给成员 B 的 AI Prompt

```text
我正在参与数据库课程设计项目：TPC CommerceInsight。

请严格基于当前仓库中的 README.md 和 docs/ 规范文档进行分析、开发和指导，不要脱离现有设计另起炉灶。

你必须优先阅读以下内容：

1. README.md
2. docs/00_冻结文档.md
3. docs/分工指南.md
4. docs/api/api-contract.md
5. docs/api/mock-contract.md
6. docs/database/database-design.md
7. docs/database/table-contract.md
8. docs/database/tpch-query-contract.md
9. docs/database/tpcc-transaction-design.md
10. docs/integration/frontend-backend-checklist.md
11. docs/git/git协作指南.md

我的身份是“成员 B”，我的固定分支是：

dev/b-backend-service

我的固定技术方向是：

- SpringBoot 后端
- MyBatis 或 JDBC
- DTO/VO 字段映射
- 统一响应和异常处理
- 登录注册和用户管理
- 数据导入导出接口
- 业务查询接口
- TPC-H 查询接口
- TPC-C New-Order 和 Payment 事务服务
- Mock Repository

当前项目必须遵守以下关键口径：

- API 字段以 docs/api/api-contract.md 为准
- Mock 字段以 docs/api/mock-contract.md 为准
- 数据库字段是 snake_case，API JSON 字段是 lowerCamelCase
- B 负责 DTO/VO 字段映射
- C 不依赖数据库字段
- 事务必须使用 Spring 事务，成功 commit，失败 rollback
- New-Order 库存变化要能写入 stock_change_log
- B 不负责 Docker、dbgen、Python 压测和 ECharts 图表

我的工作边界是：

- 主写 backend/
- 主写 Controller、Service、Mapper、DTO、VO、统一响应、异常处理
- 先提供 Mock Repository 支持 C 并行开发
- 后续切换真实 PostgreSQL
- 给 C 提供稳定 API 和示例响应
- 给 D 提供可重复调用的接口、测试账号和后端运行说明
- 不直接修改 A 的数据库字段设计
- 不主写前端页面

请输出一份详细工作指导，必须包含：

1. 成员 B 当前负责什么，不负责什么
2. SpringBoot 推荐目录结构
3. 统一响应、异常处理、权限控制如何设计
4. Mock Repository 如何先支持 C 开发
5. API 字段如何严格对齐 api-contract.md
6. DTO/VO 如何映射数据库 snake_case 到 lowerCamelCase
7. TPC-H Q1/Q5/Q12/Q14 接口实现顺序
8. TPC-C New-Order 和 Payment 事务实现顺序
9. 如何与 A 对接 SQL 和字段语义
10. 如何与 C 做前后端联调
11. 如何给 D 提供性能测试接口
12. Git 上每天应该怎么操作

请给出按步骤执行的清单，不要泛泛建议。
```

## 十二、给成员 C 的 AI Prompt

```text
我正在参与数据库课程设计项目：TPC CommerceInsight。

请严格基于当前仓库中的 README.md 和 docs/ 规范文档进行分析、开发和指导，不要脱离现有设计另起炉灶。

你必须优先阅读以下内容：

1. README.md
2. docs/00_冻结文档.md
3. docs/分工指南.md
4. docs/product/产品需求文档.md
5. docs/architecture/架构设计文档.md
6. docs/api/api-contract.md
7. docs/api/mock-contract.md
8. docs/integration/frontend-backend-checklist.md
9. docs/integration/联调与验收清单.md
10. docs/git/git协作指南.md

我的身份是“成员 C”，我的固定分支是：

dev/c-frontend-visual

我的固定技术方向是：

- Vue 3 前端
- Element Plus 页面
- ECharts 可视化
- Axios 封装
- Mock JSON
- 登录注册、用户管理、数据导入导出页面
- 业务查询页面
- TPC-H/TPC-C 页面
- 性能分析页面
- 前后端联调和截图

当前项目必须遵守以下关键口径：

- C 只依赖 API/Mock 字段，不依赖数据库字段
- Mock 字段必须来自 docs/api/mock-contract.md
- 真实接口字段必须来自 docs/api/api-contract.md
- chartData 字段直接服务 ECharts
- 不允许因为页面字段名要求 A 修改数据库字段
- 性能图表使用 D 提供的 CSV/JSON 转换结果
- C 不负责运行压测，不负责解释数据库执行计划

我的工作边界是：

- 主写 frontend/
- 主写页面、组件、路由、状态管理、Axios、Mock JSON、ECharts
- Mock 模式下先独立完成可演示页面
- 真实接口可用后负责前后端联调
- 提供系统截图和演示路径
- 不主写 SpringBoot 接口
- 不主写 SQL 和数据库字段设计
- 不维护 Docker 和正式导入脚本

请输出一份详细工作指导，必须包含：

1. 成员 C 当前负责什么，不负责什么
2. Vue 前端推荐目录结构
3. 页面清单和每个页面的核心字段
4. 如何使用 mock-contract.md 生成 Mock JSON
5. 如何封装 Axios 并支持 Mock/真实接口切换
6. ECharts 图表如何使用 chartData
7. 如何与 B 做前后端联调
8. 如何使用 D 的性能结果展示图表
9. 如何准备截图和验收演示路径
10. Git 上每天应该怎么操作

请给出按步骤执行的清单，不要泛泛建议。
```

## 十三、给成员 D 的 AI Prompt

```text
我正在参与数据库课程设计项目：TPC CommerceInsight。

请严格基于当前仓库中的 README.md 和 docs/ 规范文档进行分析、开发和指导，不要脱离现有设计另起炉灶。

你必须优先阅读以下内容：

1. README.md
2. docs/00_冻结文档.md
3. docs/分工指南.md
4. docs/database/sql-execution-and-import.md
5. docs/database/database-design.md
6. docs/database/table-contract.md
7. docs/database/index-design.md
8. docs/database/explain-baseline-analysis.md
9. docs/integration/database-deploy-checklist.md
10. docs/integration/联调与验收清单.md
11. docs/git/git协作指南.md

我的身份是“成员 D”，我的固定分支是：

dev/d-devops-performance-report

我的固定技术方向是：

- Docker PostgreSQL
- dbgen 和 TPC-H 数据生成
- SF=0.1/SF=0.6/SF=1 数据导入
- COPY 脚本
- 数据库重置和行数统计
- EXPLAIN ANALYZE 正式结果
- Python 并发性能测试
- 索引前后对比实验
- 性能 CSV/JSON
- 报告整合、README、提交包检查

当前项目必须遵守以下关键口径：

- sql/ 是唯一 SQL 源资产目录
- db/init/ 只作为 Docker 初始化入口
- 00_run_sql_assets.sh 只引用 /sql/V*.sql
- 00_run_sql_assets.sh 必须使用 LF 换行，并 chmod +x
- 开发初始化模式和正式导入模式 SQL 顺序不同
- 第 3 天开始 SF=1，但必须确保 SF=0.1 完整完成
- SF=0.6/SF=1 第 4-5 天冻结
- D 不替 A 改 SQL，不替 B 修接口，不替 C 写页面

我的工作边界是：

- 主写 db/、test/、report/ 中与环境、导入、性能、报告整合相关的内容
- 维护 docker-compose.yml 和 db/init/00_run_sql_assets.sh
- 维护 reset/load/count 等脚本
- 执行正式数据导入、行数统计、EXPLAIN、性能测试
- 给 C 提供性能 CSV/JSON
- 给报告提供截图、日志、实验结论
- 不直接维护第二套业务 SQL
- 不代替其他成员完成专业模块

请输出一份详细工作指导，必须包含：

1. 成员 D 当前负责什么，不负责什么
2. Docker PostgreSQL 推荐目录和配置
3. 00_run_sql_assets.sh 如何编写、检查 LF 和 chmod +x
4. 开发初始化模式和正式导入模式的执行顺序
5. SF=0.1、SF=0.6、SF=1 的推进策略
6. COPY 导入、行数统计和日志保存方法
7. EXPLAIN ANALYZE 和索引前后对比如何组织
8. Python 并发测试脚本如何输出 CSV/JSON
9. 如何与 A/B/C 收集报告材料
10. 如何检查最终提交包
11. Git 上每天应该怎么操作

请给出按步骤执行的清单，不要泛泛建议。
```

