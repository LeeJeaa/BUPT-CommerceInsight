# DBMS 选型说明

> 决策日期：2026-07-06  
> 决策人：全组，A 记录  
> 最终选择：**Docker PostgreSQL 16**  
> 放弃选项：openGauss

---

## 1. 候选方案

| 方案 | 说明 |
|---|---|
| Docker PostgreSQL 16 | 官方镜像，跨平台，社区活跃 |
| openGauss | 华为发布，兼容部分 PostgreSQL 语法，国产数据库 |

---

## 2. 选择 Docker PostgreSQL 的理由

### 2.1 工具链与开发环境兼容

PostgreSQL 16 官方 Docker 镜像在 Windows / macOS / Linux 上均可一键拉取运行：

```bash
docker run --name tpc-commerce-postgres \
  -e POSTGRES_USER=tpc_admin \
  -e POSTGRES_PASSWORD=tpc_password \
  -e POSTGRES_DB=tpc_commerce \
  -p 5432:5432 \
  -d postgres:16
```

四名成员使用不同操作系统（Windows 11 / macOS），使用 Docker 可以保证数据库版本统一，避免本地安装版本差异导致 SQL 兼容问题。

### 2.2 TPC-H dbgen 兼容

TPC-H 官方工具 dbgen 生成的数据文件使用 `|` 分隔符，PostgreSQL 的 `COPY` 命令直接支持：

```sql
COPY lineitem FROM '/data/lineitem.tbl' WITH (FORMAT TEXT, DELIMITER '|');
```

openGauss 的 `COPY` 语法与 PostgreSQL 有差异，在容器内使用 `\COPY` 还需要额外适配，增加 D 的工作量。

### 2.3 JDBC / MyBatis 直接支持

Spring Boot 通过标准 PostgreSQL JDBC 驱动连接：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tpc_commerce
    username: tpc_admin
    password: tpc_password
    driver-class-name: org.postgresql.Driver
```

openGauss 使用定制 JDBC 驱动，部分 MyBatis 功能需要额外配置，增加 B 的开发风险。

### 2.4 标准 SQL 特性完整

本项目用到的 PostgreSQL 特性全部有官方文档支持：

| 特性 | 用途 |
|---|---|
| `PREPARE` / `EXECUTE` | TPC-C SQL 流程验证 |
| `RETURNING` | New-Order 获取插入后 orderId |
| `ON CONFLICT DO NOTHING/UPDATE` | 导入清洗主键冲突处理 |
| `set_config()` / `current_setting()` | 触发器获取事务上下文 |
| `CREATE FUNCTION ... LANGUAGE plpgsql` | 触发器函数 |
| `CREATE FUNCTION ... LANGUAGE sql` | 存储过程（可返回表） |
| `EXPLAIN (ANALYZE, BUFFERS)` | 索引对比与执行计划分析 |

openGauss 对 `set_config` 和 `current_setting` 的支持与 PostgreSQL 有细微差异，可能影响 `trg_stock_quantity_change` 触发器读取事务上下文。

### 2.5 Docker 镜像稳定性与文档

`postgres:16` 是官方维护镜像，每周更新 patch，DockerHub 月下载量超过 10 亿次。相比之下，openGauss 容器镜像更新频率低，中文文档不完整，英文文档更少，D 遇到问题时排查成本更高。

---

## 3. 放弃 openGauss 的原因

| 原因 | 影响 |
|---|---|
| JDBC 驱动定制，与标准 PostgreSQL JDBC 不兼容 | B 需要单独配置驱动和连接池 |
| `COPY` 语法有差异，`\COPY` 在容器中行为不同 | D 导入 SF=1 数据需要额外适配 |
| `set_config` / `current_setting` 支持不确定 | A 的触发器事务上下文可能无法正常工作 |
| Docker 镜像更新慢，社区支持少 | 出问题难以快速查找解决方案 |
| 课程验收环境未强制要求国产数据库 | 没有必要承担额外兼容风险 |

---

## 4. Docker Compose 配置（由 D 维护）

D 在 `db/docker-compose.yml` 中使用以下配置：

```yaml
services:
  postgres:
    image: postgres:16
    container_name: tpc-commerce-postgres
    environment:
      POSTGRES_USER: tpc_admin
      POSTGRES_PASSWORD: tpc_password
      POSTGRES_DB: tpc_commerce
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ../sql:/sql:ro
      - ./init:/docker-entrypoint-initdb.d:ro

volumes:
  postgres_data:
```

A 负责 `sql/` 目录，D 负责 `db/docker-compose.yml` 和 `db/init/00_run_sql_assets.sh`。

---

## 5. 结论

**本项目统一使用 Docker PostgreSQL 16，不使用 openGauss。**

原因一句话：PostgreSQL 16 的 JDBC 驱动、COPY 命令、触发器函数、EXPLAIN 工具与本项目的 Spring Boot + dbgen + 性能测试方案完全兼容，而 openGauss 在关键路径上均存在额外适配风险，不值得引入。
