# Docker、迁移与性能测试交付说明

## 交付范围

- Docker PostgreSQL 16 与版本化 SQL 初始化。
- 已有数据库 volume 的非破坏性升级。
- 带显式确认的数据库重置。
- dbgen 数据生成、TPC-H COPY 导入和行数统计。
- Q1/Q5/Q12/Q14 索引前后 `EXPLAIN ANALYZE`。
- TPC-H/TPC-C 独立 HTTP 并发测试、CSV/JSON 归档和性能结果入库。

## 推荐执行顺序

```powershell
# 1. 环境检查
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\verify-environment.ps1

# 2A. 已有 volume：只升级，不删除数据
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\migrate-db.ps1

# 2B. 只有确需重建时才执行
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\reset-db.ps1 -Force

# 3. 生成并导入 SF=0.1
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\generate-tpch.ps1 -ScaleFactor 0.1
$env:DEFER_POST_COPY_ASSETS = "true"
$env:LOAD_SAMPLE_DATA = "false"
$env:LOAD_BASELINE_INDEXES = "false"
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\reset-db.ps1 -Force
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\load-tpch.ps1 -ScaleFactor 0.1 -DataDir <绝对数据目录> -FileExtension tbl -FinalizeSchema
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\count-tables.ps1

# 4. 先无索引、后有索引，最终恢复标准索引
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\run-tpch-explain.ps1 -IndexMode without_index -Query all -ScaleFactor 0.1
powershell -NoProfile -ExecutionPolicy Bypass -File db\scripts\run-tpch-explain.ps1 -IndexMode with_index -Query all -ScaleFactor 0.1

# 5. 启动 dev 后端后执行 HTTP 压测
python test\tpch_concurrent_test.py --query all --threads 4 --requests 20 --username admin --password admin123 --scale-factor 0.1
python test\tpcc_concurrent_test.py --transaction all --threads 4 --requests 20 --username admin --password admin123 --scale-factor 0.1
```

## 连接口径

```text
container: tpc-commerce-postgres
database:  tpc_commerce
user:      tpc_admin
port:      5432
backend:   jdbc:postgresql://localhost:5432/tpc_commerce
```

不同设备各自开发时，`localhost` 只表示当前设备。每台设备本地运行同一套 Docker 数据库时可以使用上述配置；连接另一台设备时必须改成目标设备的局域网地址并配置网络访问。

## 结果位置

- 导入与迁移日志：`report/import_logs/`
- 执行计划：`report/explain_plans/`
- 性能结果：`report/performance/`
- 联调截图：`report/screenshots/`
- 详细开发记录：`docs/docs-D/D实验开发记录.md`
