# 演示流程

## 1. 启动真实后端

真实联调和 D 阶段验收必须显式选择 `dev` 或 `prod` profile，不能依赖默认 Mock。

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

开发库连接可通过环境变量覆盖：

```text
DB_URL=jdbc:postgresql://localhost:5432/tpc_commerce
DB_USERNAME=tpc_admin
DB_PASSWORD=<local-password>
```

## 2. 启动前端真实接口模式

```powershell
cd frontend
$env:VITE_USE_MOCK="false"
npm.cmd run dev
```

访问：

```text
http://localhost:5173
```

## 3. 登录账号

```text
管理员：admin / admin123
普通用户：user1 / user123
```

## 4. 验收路径

1. 登录管理员账号，进入系统总览，确认 Dashboard 行数来自 `/api/dashboard/summary`。
2. 打开“客户查询”，按国家筛选，确认分页字段和表格字段正常。
3. 打开“订单收入查询”，输入日期范围，确认 revenue 正常展示。
4. 打开“零部件供应查询”，搜索 `Supplier`，确认调用 `/api/query/part-supplier`。
5. 打开“TPC-H 分析”，运行 Q5，确认图表和返回记录正常。
6. 打开“TPC-C New-Order”，提交订单，确认状态为 `committed`。
7. 打开“TPC-C Payment”，提交支付，确认状态为 `committed`。
8. 在数据库检查 `transaction_log`，成功记录应为 `committed`；构造库存不足或不存在上下文时，应保留 `rolled_back` 或 `failed` 记录。
9. 用普通用户访问用户管理，确认返回 403。
10. 打开性能结果页，确认读取真实 `performance_result` 或演示数据口径一致。

## 5. Mock 演示模式

仅前端独立演示或无数据库环境时使用：

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=mock
```

```powershell
cd frontend
$env:VITE_USE_MOCK="true"
npm.cmd run dev
```
