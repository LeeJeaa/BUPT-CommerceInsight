# Git 协作指南

## 1. 分支策略

主分支：

```text
main
```

日常集成分支：

```text
develop
```

成员开发分支：

```text
dev/a-database-sql
dev/b-backend-service
dev/c-frontend-visual
dev/d-devops-performance-report
```

紧急修复分支：

```text
fix/<short-description>
```

分支用途：

| 分支 | 用途 |
|---|---|
| `main` | 最终稳定验收版，只放可演示、可提交版本 |
| `develop` | 日常集成分支，四个人功能最终先合并到这里 |
| `dev/a-database-sql` | A 的数据库 SQL、表结构、触发器、存储过程、索引方案分支 |
| `dev/b-backend-service` | B 的 SpringBoot、MyBatis、接口、事务服务分支 |
| `dev/c-frontend-visual` | C 的 Vue、Element Plus、ECharts、Mock、前后端联调分支 |
| `dev/d-devops-performance-report` | D 的 Docker、数据导入脚本、性能测试、报告整合分支 |
| `fix/<short-description>` | 紧急修复分支 |

## 2. 提交规范

格式：

```text
<type>(<scope>): <summary>
```

类型：

| type | 含义 |
|---|---|
| docs | 文档 |
| feat | 新功能 |
| fix | 修复 |
| sql | SQL 和数据库资产 |
| api | 接口契约或后端接口 |
| ui | 前端页面 |
| test | 测试和性能脚本 |
| chore | 构建、配置、脚本 |

示例：

```text
sql(database): add stock_change_log table
api(tpch): add q5 response dto
ui(performance): render latency throughput charts
docs(freeze): clarify formal import order
```

## 3. 合并规则

```text
每个人在自己的 `dev/*` 分支开发。
跨成员字段变更先改 docs 契约。
提交 PR 前必须自测自己负责的最小闭环。
不要在自己的 PR 中修改别人负责模块，除非已沟通。
成员分支先合并到 `develop`。
`main` 分支只合并从 `develop` 验收通过的可演示、可提交版本。
```

## 4. PR 检查清单

通用：

```text
是否影响 API 字段
是否影响数据库字段
是否影响 Mock JSON
是否影响 Docker 初始化
是否影响正式导入
是否更新对应 docs
是否写明自测结果
```

A 的 PR：

```text
sql/V*.sql 是否按顺序执行
是否符合 snake_case
是否记录表/字段/约束变更
是否说明是否影响 B DTO/Mapper
```

B 的 PR：

```text
接口是否符合 api-contract.md
Mock Repository 是否符合 mock-contract.md
事务是否 rollback
是否记录 elapsedMs
```

C 的 PR：

```text
Mock 字段是否符合 mock-contract.md
页面是否能在 Mock 模式下独立展示
Axios 调用路径是否符合 api-contract.md
图表字段是否来自 chartData
```

D 的 PR：

```text
Docker 是否可启动
00_run_sql_assets.sh 是否 LF + chmod +x
是否区分开发初始化和正式导入
是否保存行数/导入/性能结果
```

## 5. 冲突处理

```text
docs/api 冲突：B 牵头，C 共同确认。
docs/database 或 sql 冲突：A 牵头，D 确认执行顺序。
Docker 和导入脚本冲突：D 牵头，A 确认 SQL 兼容。
前端字段冲突：以 api-contract.md 和 mock-contract.md 为准。
```

## 6. 禁止事项

```text
禁止直接向 main 推送未验证代码。
禁止在 db/init/ 手写第二套业务 SQL。
禁止 C 直接依赖数据库字段。
禁止 A 为了前端显示名修改数据库字段。
禁止修改别人成果后不说明。
禁止提交大规模 dbgen 数据文件。
```
