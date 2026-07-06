# 数据库基线环境

> 该目录由成员 A 提供初版，用于验证 `sql/` 源资产可通过 Docker PostgreSQL 初始化。正式 dbgen、COPY 和性能测试环境仍由 D 负责。

## 1. 文件

```text
db/docker-compose.yml
db/init/00_run_sql_assets.sh
```

`00_run_sql_assets.sh` 只引用 `/sql/V*.sql`，不在 `db/init/` 维护第二套业务 SQL。

## 2. 启动

在仓库根目录执行：

```powershell
docker compose -f db/docker-compose.yml up -d
```

查看状态：

```powershell
docker compose -f db/docker-compose.yml ps
```

查看日志：

```powershell
docker logs tpc-commerce-postgres
```

## 3. 连接

容器内连接：

```powershell
docker exec -it tpc-commerce-postgres psql -U tpc_admin -d tpc_commerce
```

本机安装 `psql` 时连接：

```powershell
psql "postgresql://tpc_admin:tpc_password@localhost:5432/tpc_commerce"
```

## 4. 重置

```powershell
docker compose -f db/docker-compose.yml down -v
docker compose -f db/docker-compose.yml up -d
```

推荐使用 `down -v` 清理 volume 后再启动，确保样例数据、COPY 数据和验证日志从干净状态开始。`V4__add_constraints.sql` 已支持重复执行，已存在的约束会自动跳过。

## 5. 手动 EXPLAIN

```powershell
docker exec -it tpc-commerce-postgres psql -U tpc_admin -d tpc_commerce -f /sql/V12__explain_baseline.sql
```

## 6. 可选事务演示数据

V11 只加载基础样例和回滚验证，不持久化订单事务。需要给 B/C 查看已提交 New-Order、Payment 结果时执行：

```powershell
docker exec -it tpc-commerce-postgres psql -U tpc_admin -d tpc_commerce -f /sql/demo_transaction_data.sql
```
