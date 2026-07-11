# 联调自测包（A → B/C/D）

> 适用对象：B、C、D 均可使用  
> 目的：当联调出现问题时，B/C/D 可以用本文档的检查步骤判断"是 SQL 问题、接口问题还是页面问题"，并确认问题责任方。

---

## 1. 问题定位决策树

```
联调发现数据不对 / 报错
│
├─ 页面没有数据 / 显示空白
│   ├─ 打开浏览器 Network → 请求是否发出？
│   │   ├─ 未发出 → C 的 Axios / 路由问题
│   │   └─ 已发出 → 看响应 code
│   │       ├─ 401/403 → 登录 token 或权限问题（B）
│   │       ├─ 404    → 接口路径写错（C 或 B）
│   │       ├─ 500    → 后端异常（B），查后端日志
│   │       └─ 200 但 data 为空 → 进入"后端返回空"分支
│
├─ 后端返回空（data.records = []）
│   ├─ 用样例参数直接调 B 接口（Postman / curl）
│   │   ├─ 同样返回空 → 后端问题
│   │   │   ├─ 用 psql 直接跑同参数的 SQL
│   │   │   │   ├─ SQL 有结果 → B 的 Mapper/参数绑定问题
│   │   │   │   └─ SQL 也无结果 → A 的 SQL 逻辑或参数范围问题
│   │   └─ Postman 有结果 → C 的请求参数格式问题
│
├─ 数据显示乱 / 字段缺失
│   ├─ 后端返回 JSON 中字段是否存在？
│   │   ├─ 字段缺失 → B 的 DTO 未映射或 SELECT 少了别名（B → 参考 backend-adapter-pack.md）
│   │   └─ 字段存在 → C 的 prop / chartData 字段名写错
│
├─ 事务报错 / 回滚
│   ├─ 后端日志中有 Spring 事务异常信息 → B
│   ├─ 日志显示 SQL 执行失败
│   │   ├─ 库存不足（RETURNING 0 行） → 正常业务流程，B 正确抛异常
│   │   └─ 约束违反 / 外键错误 → 测试数据问题，先用 V11 样例数据验证
│
└─ 性能数据图表不对
    ├─ D 的 CSV/JSON 字段名与 mock-contract.md 不一致 → D 修字段名
    └─ C 的 ECharts data 绑定字段名错误 → C 修 chartData 取值路径
```

---

## 2. psql 快速验证 SQL（B/D 均可用）

> 进入容器：`docker exec -it tpc-commerce-postgres psql -U tpc_admin -d tpc_commerce`

### 2.1 验证 Q1（样例数据下有结果）

```sql
SELECT l_returnflag, l_linestatus, COUNT(*) AS count_order
FROM lineitem
WHERE l_shipdate <= '1998-12-01'
GROUP BY l_returnflag, l_linestatus
ORDER BY l_returnflag, l_linestatus;
```

预期：返回 3 行以上（A/N/R × O/F 组合）

---

### 2.2 验证 Q5（样例数据下需要存在区域数据）

```sql
-- 先确认 ASIA 区域存在
SELECT r_regionkey, r_name FROM region WHERE TRIM(r_name) = 'ASIA';

-- 再跑 Q5
SELECT TRIM(n.n_name) AS nation_name,
       SUM(l.l_extendedprice * (1 - l.l_discount)) AS revenue
FROM customer c
JOIN orders o   ON c.c_custkey   = o.o_custkey
JOIN lineitem l ON l.l_orderkey  = o.o_orderkey
JOIN supplier s ON s.s_suppkey   = l.l_suppkey
JOIN nation n   ON n.n_nationkey = c.c_nationkey
               AND n.n_nationkey = s.s_nationkey
JOIN region r   ON r.r_regionkey = n.n_regionkey
WHERE TRIM(r.r_name) = 'ASIA'
  AND o.o_orderdate >= '1993-01-01'
  AND o.o_orderdate <  '1998-01-01'
GROUP BY n.n_name
ORDER BY revenue DESC;
```

若样例数据量小但 ASIA 区域存在，应返回至少 1 行。

---

### 2.3 验证 TPC-C 事务数据（B 联调前确认样例数据正确）

```sql
-- warehouse
SELECT w_id, w_tax FROM warehouse LIMIT 3;

-- district
SELECT d_w_id, d_id, d_next_o_id FROM district LIMIT 3;

-- customer
SELECT c_w_id, c_d_id, c_id, c_balance FROM tpcc_customer LIMIT 3;

-- item + stock
SELECT i.i_id, i.i_name, s.s_quantity
FROM item i JOIN stock s ON s.s_i_id = i.i_id
WHERE s.s_w_id = 1
LIMIT 5;
```

若以上查询返回空，说明 `sql/V11__sample_data.sql` 未执行，或执行顺序有误。

---

### 2.4 验证触发器是否生效（B 联调 New-Order 后检查）

```sql
-- 执行一次 New-Order 后，stock_change_log 应有记录
SELECT * FROM stock_change_log ORDER BY created_at DESC LIMIT 5;

-- transaction_log 也应有记录
SELECT transaction_id, transaction_type, status, elapsed_ms
FROM transaction_log ORDER BY created_at DESC LIMIT 5;
```

若 `stock_change_log` 为空但 New-Order 成功，说明事务上下文未设置：

```sql
-- B 需要在事务内先执行：
SELECT set_config('app.transaction_id', 'test-001', true);
SELECT set_config('app.change_type', 'new_order', true);
```

---

### 2.5 验证导入表结构（D 在 COPY 前确认）

```sql
-- 检查 orders 字段顺序（需与 dbgen 输出一致）
SELECT column_name, data_type, ordinal_position
FROM information_schema.columns
WHERE table_name = 'orders'
ORDER BY ordinal_position;

-- 检查 lineitem 字段顺序
SELECT column_name, data_type, ordinal_position
FROM information_schema.columns
WHERE table_name = 'lineitem'
ORDER BY ordinal_position;
```

---

## 3. 接口冒烟测试（B 完成接口后发给 C/D）

B 验证接口可用后，给 C/D 以下 curl 示例：

```bash
# 1. 登录，获取 token
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | python -m json.tool

# 2. Q5 查询
curl -s "http://localhost:8080/api/tpch/q5?regionName=ASIA&startDate=1994-01-01&endDate=1995-01-01" \
  -H "Authorization: Bearer <token>" | python -m json.tool

# 3. New-Order
curl -s -X POST http://localhost:8080/api/tpcc/new-order \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"warehouseId":1,"districtId":1,"customerId":1,"items":[{"itemId":1001,"quantity":1}]}' \
  | python -m json.tool

# 4. Payment
curl -s -X POST http://localhost:8080/api/tpcc/payment \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"warehouseId":1,"districtId":1,"customerId":1,"amount":25.00}' \
  | python -m json.tool
```

---

## 4. 字段一致性核查表

B 完成接口后，C 用以下表核查返回字段名是否与 mock-contract.md 一致：

| 接口 | 预期字段 | 检查方法 |
|---|---|---|
| `GET /api/tpch/q5` | `nationName`, `revenue` | Postman 查看 records[0] key |
| `GET /api/tpch/q12` | `shipMode`, `highLineCount`, `lowLineCount` | 同上 |
| `GET /api/tpch/q14` | `promoRevenuePercent` | 同上 |
| `GET /api/tpch/q1` | `returnFlag`, `lineStatus`, `sumCharge`, `avgPrice` | 同上 |
| 所有 TPC-H | `elapsedMs`, `rowCount`, `chartData` | 同上 |
| 导入任务 | `taskId`, `totalRows`, `successRows`, `failedRows` | 同上 |
| 导入错误 | `lineNumber`, `fieldName`, `fieldValue`, `errorReason` | 同上 |
| New-Order | `transactionId`, `status`, `elapsedMs` | 同上 |
| Payment | `transactionId`, `newBalance`, `elapsedMs` | 同上 |

禁止字段名分裂示例：

```text
✗ B 返回 nation_name，C 写 row.nation_name（snake_case 意外漏映射）
✗ B 返回 elapsedMs，C 写 row.timeCost
✗ B 返回 chartData.highSeries，C 写 chartData.highLineData
```

---

## 5. 三层分工故障责任边界

| 现象 | 先查 | 责任方 |
|---|---|---|
| 页面 Network 无请求 | C 的路由/事件绑定 | C |
| 请求发出但 404 | 接口路径，B 的 Controller | B 或 C |
| 请求发出但 401 | token 有效期，B 的鉴权过滤器 | B |
| 请求发出但 500 | B 的后端日志（Spring 异常） | B |
| 响应 200 但 data.records 为空 | psql 直接跑 SQL | A（SQL 逻辑）或 B（Mapper 参数） |
| 响应字段存在但值错误 | B 的 DTO 字段映射 | B |
| 响应字段存在但 C 显示不对 | C 的 prop 或 chartData 绑定 | C |
| 事务回滚但日志无记录 | B 是否设置 app.transaction_id | B（参考 backend-adapter-pack.md §3.1） |
| EXPLAIN 结果与样例差别大 | 是否在 SF=1 数据上执行 | D（重跑），A（解释） |
| 性能图表数据格式错 | D 的 CSV/JSON 字段 vs mock-contract | D |

---

## 6. 测试环境前置检查

B/C/D 联调前，请确认以下均已就绪：

```text
□ Docker PostgreSQL 容器已启动（docker ps 确认 tpc-commerce-postgres 运行中）
□ sql/V1~V4 已执行（表结构存在）
□ sql/V8 已执行（触发器存在）
□ sql/V9 已执行（存储过程存在）
□ sql/V11 已执行（样例数据存在，warehouse/item/stock/customer 均有行）
□ 后端 SpringBoot 已启动（默认 localhost:8080）
□ 测试账号可以登录（admin / admin123）
□ 前端已启动（默认 localhost:5173 或 localhost:3000）
```

快速验证命令：

```sql
-- 检查样例数据
SELECT 'warehouse' AS t, COUNT(*) FROM warehouse
UNION ALL SELECT 'item',         COUNT(*) FROM item
UNION ALL SELECT 'stock',        COUNT(*) FROM stock
UNION ALL SELECT 'tpcc_customer',COUNT(*) FROM tpcc_customer
UNION ALL SELECT 'orders',       COUNT(*) FROM orders
UNION ALL SELECT 'lineitem',     COUNT(*) FROM lineitem;
```

所有表行数 > 0 才可进行联调。若有表为 0，重新检查 V11 的执行顺序（需在 V4 约束之后执行）。
