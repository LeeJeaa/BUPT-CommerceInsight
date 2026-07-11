# 文档索引

| 文档 | 用途 |
|---|---|
| `00_冻结文档.md` | 全组冻结基线，跨成员变更以此为准 |
| `分工指南.md` | 四人分工、每日计划、交付标准 |
| `product/产品需求文档.md` | 产品目标、角色、功能和验收指标 |
| `architecture/架构设计文档.md` | 系统分层、数据流、技术选型 |
| `api/api-contract.md` | 后端真实接口规范 |
| `api/mock-contract.md` | 前端 Mock 和后端 Mock Repository 字段规范 |
| `database/database-design.md` | 数据库命名、表、事务、索引规范 |
| `database/table-contract.md` | 表清单、应用辅助表和变更影响 |
| `database/naming-convention.md` | 数据库对象命名细则 |
| `database/tpch-query-contract.md` | TPC-H Q1/Q5/Q12/Q14 查询契约 |
| `database/tpcc-transaction-design.md` | TPC-C New-Order 与 Payment 事务流程 |
| `database/import-cleaning-rules.md` | 系统演示导入清洗规则 |
| `database/triggers-and-procedures.md` | 触发器和存储过程规范 |
| `database/index-design.md` | 索引方案与对比实验要求 |
| `database/explain-baseline-analysis.md` | EXPLAIN 分析保存和解读规范 |
| `database/sql-execution-and-import.md` | SQL 执行顺序、开发初始化、正式导入规范 |
| `git/git协作指南.md` | 分支、提交、PR、冲突处理规范 |
| `integration/联调与验收清单.md` | 联调和最终验收检查清单 |
| `integration/frontend-backend-checklist.md` | 前后端联调检查 |
| `integration/database-deploy-checklist.md` | 数据库 Docker 部署检查 |
| `docs-A/` | 成员 A 数据库基线、交付和验证文档 |
| `docs-B/README.md` | 成员 B 后端交付、修复、声明和联调文档索引 |
| `docs-D/D成员交付说明.md` | 成员 D 部署、测试和报告交付说明 |

使用顺序：

```text
先读 00_冻结文档.md
再按自己的角色阅读专项文档
开发前确认 API/Mock/数据库契约
提交前按 Git 协作指南和联调清单自查
```
