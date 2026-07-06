# SQL 执行与数据导入规范

## 1. SQL 资产原则

```text
sql/ 是唯一 SQL 源目录。
db/init/ 只是 Docker 初始化入口。
业务 DDL、约束、触发器、存储过程、索引必须先进入 sql/。
D 只能在 db/init/00_run_sql_assets.sh 中引用 sql/V*.sql。
```

## 2. 脚本顺序

推荐 SQL 文件：

```text
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
```

## 3. 开发初始化模式

用途：A/B/C 快速开发和样例联调。

执行顺序：

```text
V1 -> V2 -> V3 -> V4 -> V8 -> V9 -> V10 -> 可选 V11
```

特点：

```text
不导入大规模 TPC-H 数据
可以插入少量样例数据
优先脚本简单和启动速度
适合后端 Mapper、前端 Mock 切真实接口前验证
```

## 4. 正式导入模式

用途：D 构建验收、EXPLAIN 和性能测试数据库。

执行顺序：

```text
V1 -> V2 -> V3 -> COPY TPC-H 数据 -> V4 -> V8 -> V9 -> V10 -> 行数统计 -> EXPLAIN
```

特点：

```text
先保证 SF=0.1 完整导入
SF=0.6/SF=1 在第 4-5 天冻结
大表 COPY 建议早于外键和索引
导入结束后必须记录行数、耗时和日志
```

## 5. 00_run_sql_assets.sh 要求

脚本模板：

```bash
#!/usr/bin/env bash
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<'SQL'
\i /sql/V1__create_tpch_tables.sql
\i /sql/V2__create_tpcc_tables.sql
\i /sql/V3__create_app_tables.sql
\i /sql/V4__add_constraints.sql
\i /sql/V8__triggers.sql
\i /sql/V9__procedures.sql
\i /sql/V10__indexes_baseline.sql
SQL
```

提交前检查：

```bash
dos2unix db/init/00_run_sql_assets.sh
chmod +x db/init/00_run_sql_assets.sh
git ls-files --stage db/init/00_run_sql_assets.sh
```

如果没有 `dos2unix`，可以用编辑器把换行格式改为 LF。

## 6. 系统演示导入与正式导入区别

| 类型 | 负责人 | 入口 | 目的 |
|---|---|---|---|
| 系统演示导入 | B/C，A 提供规则 | Web 页面和 API | 展示批量导入、清洗、错误日志 |
| 正式数据库导入 | D，A 保证结构兼容 | dbgen + COPY 脚本 | 构建 TPC-H 正式数据和性能测试环境 |

两者不能混用。Web 导入不承担 SF=1 初始化任务；COPY 导入不替代页面导入功能。

## 7. 数据规模冻结

```text
第 2 天：完成 SF=0.1 生成和导入验证。
第 3 天：开始 SF=1，确保 SF=0.1 完成。
第 4-5 天：冻结 SF=0.6/SF=1 正式数据规模。
```

如 SF=1 因机器性能不可控，SF=0.6 可作为正式性能实验保底，但报告中必须如实记录规模。

