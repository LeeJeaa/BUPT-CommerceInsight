# 2026-07-08 全量联调记录

## 联调范围

- 前端：`http://localhost:5173`，`VITE_USE_MOCK=false`
- 后端：`http://localhost:8080`，Spring Boot `dev` profile
- 数据库：Docker Desktop 中的 PostgreSQL 16，库名 `tpc_commerce`
- 数据：课程 TPC-H SF0.2 数据已 COPY 导入，TPC-C 最小事务前置数据已补齐

## 环境修复

- 修复本机 JDK 路径 `C:\Users\33461\.jdk\jdk-17.0.16`，恢复缺失的 `lib\tzdb.dat`。
- 后端确认使用该 JDK 启动，`/api/health` 返回 200。
- 前端确认通过 Vite 启动，并通过代理访问真实后端接口。

## 补充数据

正式库已有 TPC-H 数据，不能重复执行 `sql/V11__sample_data.sql`，否则会插入重复的 TPC-H 主键。联调时仅补充：

- `warehouse`
- `district`
- `tpcc_customer`
- `item`
- `stock`
- `performance_result`

临时补数脚本：

```text
tmp/runtime/seed-integration-data.sql
```

补数后关键表行数：

| 表 | 行数 |
|---|---:|
| warehouse | 1 |
| district | 1 |
| tpcc_customer | 1 |
| item | 2 |
| stock | 2 |
| performance_result | 7 |
| app_user | 2 |

## 后端接口冒烟结果

执行脚本：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tmp\runtime\run-backend-full-smoke.ps1
```

最终结果：

```text
total=19
failed=0
```

结果文件：

```text
report/integration_logs/20260708_223039_backend_full_smoke.json
```

覆盖接口：

- 登录与健康检查
- 看板汇总
- 用户列表
- 业务查询：客户、订单收入、零部件供应
- TPC-H：Q1、Q5、Q12、Q14
- TPC-C：New-Order、Payment
- 性能结果：TPC-H、TPC-C
- 导入任务、导入错误日志
- 导出 CSV

## 前端页面联调结果

执行方式：Playwright 访问真实前端页面，登录后逐页验证。

结果文件：

```text
report/integration_logs/20260708_2231_frontend_full_integration.json
```

结论：

```text
screenshots=11
failedApi=0
download=orders.csv
```

截图文件：

```text
report/screenshots/20260708_2231_integration_full_01_dashboard.png
report/screenshots/20260708_2231_integration_full_02_users.png
report/screenshots/20260708_2231_integration_full_03_query_customers.png
report/screenshots/20260708_2231_integration_full_04_query_order_revenue.png
report/screenshots/20260708_2231_integration_full_05_query_part_supplier.png
report/screenshots/20260708_2231_integration_full_06_tpch_q5.png
report/screenshots/20260708_2231_integration_full_07_tpcc_new_order.png
report/screenshots/20260708_2231_integration_full_08_tpcc_payment.png
report/screenshots/20260708_2231_integration_full_09_import_errors.png
report/screenshots/20260708_2231_integration_full_10_export.png
report/screenshots/20260708_2231_integration_full_11_performance.png
```

导出文件：

```text
report/integration_logs/20260708_2231_frontend_export_orders.csv
```

## 本次发现并修复的问题

1. TPC-H 前端默认参数仍使用旧样例数据日期，正式数据日期范围为 2015-2021，导致页面默认查询可能为空。
   - 修复：将 Q1/Q5/Q12/Q14 默认参数调整为当前正式数据可返回结果的 2020 年日期。

2. Q5 前端区域下拉缺少 `AFRICA`，而当前正式库 Q5 可验证参数为 `AFRICA + 2020-01-01 至 2021-01-01`。
   - 修复：Q5 区域下拉增加 `AFRICA`，默认值改为 `AFRICA`。

3. `PartSupplierView.vue` 标题闭合标签损坏，存在模板渲染风险。
   - 修复：将标题改为稳定文本 `Part Supplier Query` 并修复 `</h2>`。

## 验证命令

```powershell
# 前端构建
cd frontend
npm run build

# 后端全量接口冒烟
cd ..
powershell -NoProfile -ExecutionPolicy Bypass -File tmp\runtime\run-backend-full-smoke.ps1
```

验证结果：

- `npm run build`：通过
- 后端全量接口冒烟：19/19 通过
- 前端页面联调：API 失败数 0，截图 11 张，导出 CSV 成功

## 仍需关注

- 浏览器控制台存在一个静态资源 404，业务 API 无失败。后续可检查是否缺少 favicon 或静态资源引用。
- 旧文档和部分前端中文文案在当前终端显示为乱码，建议最后统一检查文件编码和页面文案。
