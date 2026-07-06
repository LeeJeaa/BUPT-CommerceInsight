# 成员 A 本地 SQL 执行说明

> 目的：让成员 A 可以独立验证 SQL 资产，不等待 B 的后端接口、C 的前端页面或 D 的正式 Docker/COPY 环境。

---

## 1. 本地验证范围

本地验证覆盖成员 A 已完成的开发初始化模式：

```text
V1 -> V2 -> V3 -> V4 -> V8 -> V9 -> V10 -> V11 -> V5 -> V6 -> V7 -> V12
```

说明：

1. `V1`、`V2`、`V3` 创建表结构。
2. `V4` 添加主键、外键、唯一约束和检查约束。
3. `V8` 创建触发器。
4. `V9` 创建存储过程。
5. `V10` 创建基线索引。
6. `V11` 插入少量样例数据。
7. `V5` 注册 TPC-H Q1/Q5/Q12/Q14 的查询模板。
8. `V6` 注册 TPC-C 事务 SQL 模板。
9. `V7` 注册导入清洗 SQL 模板。
10. `V12` 执行样例 EXPLAIN 模板。

正式导入模式由 D 负责：

```text
V1 -> V2 -> V3 -> COPY -> V4 -> V8 -> V9 -> V10 -> 行数统计 -> EXPLAIN
```

---

## 2. 推荐本地数据库

数据库类型：

```text
PostgreSQL 16
```

建议本地库名：

```text
tpc_commerce_dev
```

示例创建命令：

```bash
createdb tpc_commerce_dev
```

如果使用已有数据库，也可以直接在空 schema 中执行脚本。

---

## 3. 执行顺序

在仓库根目录执行：

```bash
psql -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f sql/V1__create_tpch_tables.sql
psql -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f sql/V2__create_tpcc_tables.sql
psql -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f sql/V3__create_app_tables.sql
psql -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f sql/V4__add_constraints.sql
psql -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f sql/V8__triggers.sql
psql -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f sql/V9__procedures.sql
psql -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f sql/V10__indexes_baseline.sql
psql -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f sql/V11__sample_data.sql
psql -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f sql/V5__tpch_queries.sql
psql -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f sql/V6__tpcc_transaction_sql.sql
psql -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f sql/V7__import_cleaning_rules.sql
psql -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f sql/V12__explain_baseline.sql
```

如果在 PowerShell 中使用完整连接串：

```powershell
psql "postgresql://tpc_admin:tpc_password@localhost:5432/tpc_commerce_dev" -v ON_ERROR_STOP=1 -f sql/V1__create_tpch_tables.sql
```

---

## 4. 查询模板验证

执行 `V5` 后，可以在同一个 psql 会话中验证：

```sql
EXECUTE tpch_q1(DATE '1995-12-31');
EXECUTE tpch_q5('ASIA', DATE '1994-01-01', DATE '1995-01-01');
EXECUTE tpch_q12('MAIL', 'SHIP', DATE '1994-01-01', DATE '1995-01-01');
EXECUTE tpch_q14(DATE '1995-09-01');
```

注意：

```text
PREPARE 语句只在当前数据库会话内有效。
B 在 MyBatis/JDBC 中接入时，应复制 SELECT 主体并替换为 Mapper 参数。
```

---

## 5. 无本机 psql 时的 Docker 验证方式

如果本机没有安装 `psql`，但 Docker 可用，可以使用临时 PostgreSQL 容器验证 A 的 SQL。

启动临时容器：

```powershell
docker run --name tpc-commerce-a-dev -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=tpc_commerce_dev -v "${PWD}:/work" -d postgres:16
```

等待数据库可用：

```powershell
docker exec tpc-commerce-a-dev pg_isready -U postgres -d tpc_commerce_dev
```

执行开发初始化脚本：

```powershell
docker exec tpc-commerce-a-dev psql -U postgres -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f /work/sql/V1__create_tpch_tables.sql
docker exec tpc-commerce-a-dev psql -U postgres -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f /work/sql/V2__create_tpcc_tables.sql
docker exec tpc-commerce-a-dev psql -U postgres -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f /work/sql/V3__create_app_tables.sql
docker exec tpc-commerce-a-dev psql -U postgres -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f /work/sql/V4__add_constraints.sql
docker exec tpc-commerce-a-dev psql -U postgres -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f /work/sql/V8__triggers.sql
docker exec tpc-commerce-a-dev psql -U postgres -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f /work/sql/V9__procedures.sql
docker exec tpc-commerce-a-dev psql -U postgres -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f /work/sql/V10__indexes_baseline.sql
docker exec tpc-commerce-a-dev psql -U postgres -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f /work/sql/V11__sample_data.sql
docker exec tpc-commerce-a-dev psql -U postgres -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f /work/sql/V5__tpch_queries.sql
docker exec tpc-commerce-a-dev psql -U postgres -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f /work/sql/V6__tpcc_transaction_sql.sql
docker exec tpc-commerce-a-dev psql -U postgres -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f /work/sql/V7__import_cleaning_rules.sql
docker exec tpc-commerce-a-dev psql -U postgres -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f /work/sql/V12__explain_baseline.sql
```

验证四条 TPC-H 查询：

```powershell
docker exec tpc-commerce-a-dev psql -U postgres -d tpc_commerce_dev -v ON_ERROR_STOP=1 -f /work/sql/V5__tpch_queries.sql -c "EXECUTE tpch_q1(DATE '1995-12-31');" -c "EXECUTE tpch_q5('ASIA', DATE '1994-01-01', DATE '1995-01-01');" -c "EXECUTE tpch_q12('MAIL', 'SHIP', DATE '1994-01-01', DATE '1995-01-01');" -c "EXECUTE tpch_q14(DATE '1995-09-01');"
```

清理临时容器：

```powershell
docker rm -f tpc-commerce-a-dev
```

---

## 6. 周期 0 完成标准

```text
README.md 和 docs/ 已同步到 A 分支。
docs/docs-A/ 已创建。
sql/ 目录已创建。
A 可以用本地 PostgreSQL 独立执行 V1 到 V12 中已完成的 SQL 资产。
```
