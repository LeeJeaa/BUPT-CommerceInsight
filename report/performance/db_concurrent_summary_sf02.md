# TPC-H SF0.2 数据库并发测试记录

> 记录用途：D 成员数据库级并发测试证据索引，不是最终报告正文。  
> 执行方式：`python test/tpch_db_concurrent_test.py` 通过 `docker exec psql` 并发提交 SQL。  
> 说明：该测试验证数据库和 SQL 并发执行能力；HTTP 接口压测需等 Java 后端在本机或联调机启动后执行 `test/performance_test.py`。

## 1. 测试参数

| 项 | 值 |
|---|---|
| 数据规模 | 课程数据集 SF0.2 口径，约 214 MB |
| 数据库 | PostgreSQL 16 Docker |
| 线程数 | 4 |
| 每组请求数 | 20 |
| 索引状态 | 已执行 `sql/V10__indexes_baseline.sql` |

## 2. 结果汇总

| 查询 | 成功数 | 失败数 | 平均延迟(ms) | P95(ms) | 吞吐量(req/s) | 结果文件 |
|---|---:|---:|---:|---:|---:|---|
| Q1 | 20 | 0 | 229.985 | 279.747 | 16.934 | `report/performance/tpch_db_q1_threads4_sf02_20260708_213844.json` |
| Q5 | 20 | 0 | 226.257 | 244.073 | 17.453 | `report/performance/tpch_db_q5_threads4_sf02_20260708_213847.json` |
| Q12 | 20 | 0 | 193.668 | 218.938 | 20.163 | `report/performance/tpch_db_q12_threads4_sf02_20260708_213849.json` |
| Q14 | 20 | 0 | 155.185 | 179.500 | 25.291 | `report/performance/tpch_db_q14_threads4_sf02_20260708_213850.json` |

## 3. 截图证据

```text
report/screenshots/20260708_213844_performance_db_q1_threads4_sf02.png
report/screenshots/20260708_213847_performance_db_q5_threads4_sf02.png
report/screenshots/20260708_213849_performance_db_q12_threads4_sf02.png
report/screenshots/20260708_213850_performance_db_q14_threads4_sf02.png
```

## 4. 待补 HTTP 压测

后端启动后继续执行：

```powershell
python test\performance_test.py --url "http://localhost:8080/api/tpch/q14?month=2020-09-01" --threads 4 --requests 20 --test-name tpch_http_q14_threads4_sf02 --scale-factor 0.2 --output report/performance/tpch_http_q14_threads4_sf02.json
```

HTTP 压测依赖：

- Java 17 可用。
- Spring Boot 后端启动。
- 后端连接当前 Docker PostgreSQL。
- 登录接口可返回 token 时，需要通过 `--header "Authorization:Bearer <token>"` 传入鉴权头。
