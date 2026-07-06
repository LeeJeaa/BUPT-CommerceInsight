# D 成员交付说明

> 分支：`dev/d-devops-performance-report`  
> 负责人：成员 D  
> 范围：Docker PostgreSQL、正式数据导入、行数统计、EXPLAIN、性能测试、报告材料

## 1. 已交付内容

### 1.1 Docker PostgreSQL 环境

- `db/docker-compose.yml`
- `db/init/00_run_sql_assets.sh`
- `db/README.md`

数据库统一口径：

```text
container: tpc-commerce-postgres
database:  tpc_commerce
user:      tpc_admin
password:  tpc_password
port:      5432
```

初始化脚本只调用 `sql/V*.sql`，不在 `db/init/` 中维护第二套业务 SQL。

### 1.2 D 侧脚本

- `db/scripts/reset-db.ps1`：重置 PostgreSQL 容器和 volume。
- `db/scripts/smoke-db.ps1`：验证数据库连接。
- `db/scripts/verify-environment.ps1`：检查 Docker、Compose、目录、LF、SQL 资产。
- `db/scripts/inspect-tpch-data.ps1`：检查课程 TPC-H 数据集的行数、列数和末尾分隔符。
- `db/scripts/load-tpch.ps1`：执行 TPC-H 数据 COPY 导入。
- `db/scripts/count-tables.sql` / `count-tables.ps1`：统计 TPC-H/TPC-C 核心表行数。
- `db/scripts/generate-tpch.ps1`：预留 dbgen 生成更大规模数据的流程。
- `db/scripts/run-row-counts.ps1`：行数统计快捷入口。

### 1.3 性能测试与报告材料

- `test/performance_test.py`：HTTP 并发压测脚本，输出 JSON/CSV。
- `report/performance/performance_mock.json`
- `report/performance/performance_mock.csv`
- `report/explain_plans/`：Q1/Q5/Q12/Q14 索引前后 EXPLAIN 结果模板。
- `report/final/report-outline.md`
- `report/final/course-guide-gap-check.md`
- `report/final/a-branch-pull-check.md`
- `report/final/d-day1-day3-status.md`

## 2. 已验证结果

### 2.1 环境验证

已在本地 Docker Desktop 中验证：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\verify-environment.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\smoke-db.ps1
```

验证结果：

- Docker daemon 可用。
- Compose 配置可解析。
- `db/init/00_run_sql_assets.sh` 为 LF 换行。
- PostgreSQL 16 容器可启动并连接。
- A 的 `sql/V*.sql` 已存在并可初始化数据库。

### 2.2 A SQL 初始化验证

已验证 A 分支 SQL 可完成新库初始化：

```text
V1 -> V2 -> V3 -> V4 -> V8 -> V9 -> V10
```

可创建 24 张 public 表，包括 TPC-H、课程最小 TPC-C、应用辅助表。

### 2.3 课程数据集导入验证

已对 `资料/tpc-h数据(2)` 做本地检查，行数与 `dbgen -s 0.2` 示例一致，且没有末尾多余 `|`。

已真实 COPY 导入成功：

| 表 | 行数 |
|---|---:|
| `region` | 5 |
| `nation` | 25 |
| `supplier` | 2000 |
| `customer` | 30000 |
| `part` | 40000 |
| `partsupp` | 160000 |
| `orders` | 300000 |
| `lineitem` | 1199969 |

## 3. 使用方式

启动数据库：

```powershell
docker compose -f db\docker-compose.yml up -d
```

重置数据库：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\reset-db.ps1
```

导入课程数据集：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\load-tpch.ps1 -ScaleFactor 0.2
```

统计行数：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\count-tables.ps1
```

运行压测脚本示例：

```powershell
python test\performance_test.py `
  --url "http://localhost:8080/api/tpch/q1?shipDate=2020-12-31" `
  --threads 4 `
  --requests 50 `
  --test-name q1_sf02 `
  --scale-factor 0.2 `
  --output report/performance/q1_sf02.json
```

## 4. 对 A/B/C 的说明

### 4.1 给 A

- D 已验证 A 的 SQL 资产可初始化 PostgreSQL。
- D 已验证当前课程数据集可 COPY 到 A 的 TPC-H 表结构。
- 需要注意：当前课程数据日期范围是 2015-2021，而部分 TPC-H 查询默认参数仍是 1994/1995/1998，会导致查询结果为空。建议 A/B 调整默认演示参数或全部参数化。

### 4.2 给 B

- 后端连接配置请使用：

```text
jdbc:postgresql://localhost:5432/tpc_commerce
username: tpc_admin
password: tpc_password
```

- 若 B 在自己电脑开发，`localhost` 指 B 自己电脑，需要本机也跑 Docker PostgreSQL。
- 若连接 D 的电脑，需要改为 D 机器的局域网 IP。

### 4.3 给 C

- C 可先用 `report/performance/performance_mock.json` 和 `.csv` 做性能图表。
- 后续 D 会用真实压测结果替换同结构数据。

## 5. 后续待办

- 生成或准备不低于 600M 的正式性能数据集。
- 在无索引和有索引两种模式下分别保存 Q1/Q5/Q12/Q14 的 EXPLAIN。
- B 接口完成后执行真实 HTTP 并发压测。
- 将最终导入日志、行数统计、EXPLAIN、性能结果整理进课程设计报告。
