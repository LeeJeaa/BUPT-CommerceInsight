# C_前端交互与可视化实现

## 1. 负责范围

成员 C 负责 `frontend/` 前端工程、Mock JSON、页面交互、ECharts 可视化、前后端联调记录和系统截图材料。

本次实现严格遵守冻结规则：

```text
C 只依赖 API/Mock 字段，不依赖数据库字段。
页面字段统一使用 lowerCamelCase。
Mock 字段来自 docs/api/mock-contract.md 和 docs/docs-A/frontend-display-pack.md。
性能图表字段通过前端适配层统一为当前 Mock/API 契约。
```

## 2. 前端工程

已创建 Vue 3 + Vite + JavaScript 工程：

```text
frontend/
  src/api/
  src/mock/
  src/router/
  src/stores/
  src/components/
  src/views/
```

技术栈：

```text
Vue 3
Vite
Element Plus
Vue Router
Pinia
Axios
ECharts
```

默认端口：`5173`。

测试账号以后端 Mock 代码为准：

```text
admin / admin123
user1 / user123
```

## 3. 已完成页面

```text
登录页
注册页
系统总览
用户管理
数据导入
数据导出
客户查询
订单收入查询
零部件供应查询 Mock 演示页
TPC-H Q1/Q5/Q12/Q14 分析页
TPC-C New-Order 页面
TPC-C Payment 页面
性能分析页面
```

页面能力：

```text
表格统一展示 records。
图表统一展示 chartData。
登录后保存 token，并在真实接口模式下自动添加 Authorization 请求头。
管理员功能通过路由 meta 标记和角色进行入口控制。
```

## 4. Mock 与真实接口切换

默认使用前端 Mock，保证不启动后端也可以演示核心页面。

真实接口联调时设置：

```text
VITE_USE_MOCK=false
```

Vite 代理配置：

```text
/api -> http://localhost:8080
```

## 5. 图表实现

已实现：

```text
Q1 汇总费用柱状图
Q5 国家收入柱状图
Q12 高/低优先级堆叠柱状图
Q14 促销收入占比饼图
订单收入柱状图
并发线程数-平均延迟/吞吐量双轴图
```

## 6. 字段一致性说明

前端页面不使用数据库字段名，例如：

```text
nation_name
ship_mode
promo_revenue_percent
successRequests
failedRequests
throughputQps
```

性能结果如果来自 D 的原始 JSON/CSV，会先转换为：

```text
successCount
failCount
throughput
chartData.latencySeries
chartData.throughputSeries
```

## 7. 运行方式

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

构建检查：

```powershell
cd frontend
npm.cmd run build
```

## 8. 后续联调与截图

真实后端启动后，需要按 `docs/integration/frontend-backend-checklist.md` 复核接口。

建议截图：

```text
report/screenshots/login.png
report/screenshots/dashboard.png
report/screenshots/import.png
report/screenshots/tpch_q5.png
report/screenshots/tpcc_new_order.png
report/screenshots/performance.png
```
