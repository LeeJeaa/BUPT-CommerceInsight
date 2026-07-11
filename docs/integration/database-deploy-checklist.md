# 数据库部署检查表

## 1. Docker 检查

```text
docker compose up -d 可启动。
PostgreSQL 端口可连接。
sql/ 挂载为 /sql。
db/init/ 挂载为 /docker-entrypoint-initdb.d。
```

## 2. init 脚本检查

```text
db/init/00_run_sql_assets.sh 使用 LF。
db/init/00_run_sql_assets.sh 有可执行权限。
脚本只引用 /sql/V*.sql。
脚本不写业务 DDL/DML。
```

## 3. 开发初始化检查

```text
V1/V2/V3/V4/V8/V9/V10 可按顺序执行。
可选 V11 样例数据可导入。
B 后端可连接。
```

## 4. 正式导入检查

```text
SF=0.1 数据完整导入。
第 4-5 天冻结 SF=0.6/SF=1。
COPY 日志保存。
行数统计保存。
EXPLAIN ANALYZE 保存。
```

