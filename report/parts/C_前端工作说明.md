# C_前端工作说明

## 1. 工作概述

成员 C 已完成电商数据管理系统前端部分，交付内容位于 `frontend/`，并补充了联调检查表和前端实现报告。

本次前端实现围绕课程验收目标展开：

```text
可独立 Mock 演示
可切换真实后端接口联调
覆盖系统管理、数据管理、业务查询、业务分析、事务分析、性能分析
统一使用 API/Mock 的 lowerCamelCase 字段
提供适合截图展示的可视化界面
```

## 2. 已完成内容

前端工程：

```text
Vue 3 + Vite + JavaScript
Element Plus
Vue Router
Pinia
Axios
ECharts
```

页面范围：

```text
登录页、注册页
系统总览
用户管理
数据导入、数据导出
客户查询、订单收入查询、零部件供应查询
TPC-H Q1/Q5/Q12/Q14 分析
TPC-C New-Order 事务
TPC-C Payment 事务
性能分析
```

核心能力：

```text
默认前端 Mock 模式，无需启动后端即可演示。
设置 VITE_USE_MOCK=false 后，通过 /api 代理联调后端。
登录后保存 token，并自动添加 Authorization 请求头。
管理员页面通过角色控制入口。
客户查询、订单收入查询支持筛选条件。
数据导出 Mock 已支持输出多行 CSV。
性能数据在前端适配为 successCount / failCount / throughput 等契约字段。
```

## 3. 页面风格说明

当前界面已完成视觉升级：

```text
整体采用中国风哑光鎏金主题。
背景使用宣纸米白、墨咖灰、茶金色。
字体优先使用楷体。
侧边栏、首页横幅、指标卡、图表面板加入轻量动效。
特定交互区域使用金元宝形状鼠标。
```

视觉调整只影响样式，不改变接口、路由、Mock 字段和业务逻辑。

## 4. 运行方式

进入前端目录：

```powershell
cd frontend
```

安装依赖：

```powershell
npm.cmd install
```

启动 Mock 演示模式：

```powershell
npm.cmd run dev
```

默认访问地址：

```text
http://localhost:5173
```

切换真实后端联调：

```powershell
$env:VITE_USE_MOCK="false"
npm.cmd run dev
```

## 5. 测试账号

```text
管理员：admin / admin123
普通用户：user1 / user123
```

## 6. 验收建议

建议按以下路径截图或演示：

```text
1. 登录页：使用 admin / admin123 登录。
2. 系统总览：查看首页指标卡、核心表行数、模块状态。
3. 数据导入：选择表名和文件，查看任务状态与错误日志区域。
4. 数据导出：选择 orders，导出 CSV，检查包含多行订单数据。
5. 客户查询：选择国家或关键词，确认表格随筛选变化。
6. 订单收入查询：调整日期范围，确认收入图表和表格变化。
7. TPC-H 分析：切换 Q1/Q5/Q12/Q14，查看参数、耗时、表格、图表。
8. TPC-C 事务：提交 New-Order 和 Payment，查看事务结果。
9. 性能分析：查看并发、成功数、失败数、延迟和吞吐量图表。
```

## 7. 构建检查

前端构建命令：

```powershell
npm.cmd run build
```

当前构建已通过。Vite 可能提示 chunk size 较大，这是由于 Element Plus 和 ECharts 体积较大，不影响课程验收运行。

## 8. 注意事项

```text
前端页面不得直接使用数据库 snake_case 字段。
Mock 和真实接口都应以 docs/api/api-contract.md、docs/api/mock-contract.md 为准。
零部件供应查询如后端暂无真实接口，可作为前端 Mock 演示模块说明。
真实后端联调结果继续记录到 docs/integration/frontend-backend-checklist.md。
```
