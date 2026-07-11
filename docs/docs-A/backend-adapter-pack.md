# 后端适配包（A → B）

> 适用对象：成员 B（SpringBoot + MyBatis/JDBC）  
> 目的：B 可以直接复制本文档中的 SQL 主体、字段映射和测试调用，无需再查阅 V5/V6 原始 SQL 文件。

---

## 1. 字段命名规则

数据库层全部 `snake_case`，API JSON 层全部 `lowerCamelCase`，B 在 DTO/VO 或 MyBatis `ResultMap` 中完成映射。TPC-H 查询 SQL 可以继续返回 `snake_case` 别名，但接口不能把这些别名直接透出给 C。

| 数据库字段 / SQL 别名 | API JSON 字段 | 说明 |
|---|---|---|
| `return_flag` | `returnFlag` | Q1 分组 |
| `line_status` | `lineStatus` | Q1 分组 |
| `sum_quantity` | `sumQuantity` | Q1 汇总 |
| `sum_base_price` | `sumBasePrice` | Q1 汇总 |
| `sum_discounted_price` | `sumDiscountedPrice` | Q1 汇总 |
| `sum_charge` | `sumCharge` | Q1 汇总 |
| `avg_quantity` | `avgQuantity` | Q1 均值 |
| `avg_price` | `avgPrice` | Q1 均值 |
| `avg_disc` | `avgDisc` | Q1 均值 |
| `count_order` | `countOrder` | Q1 行数 |
| `nation_name` | `nationName` | Q5 国家 |
| `revenue` | `revenue` | Q5 收入 |
| `ship_mode` | `shipMode` | Q12 运输方式 |
| `high_line_count` | `highLineCount` | Q12 高优先级 |
| `low_line_count` | `lowLineCount` | Q12 低优先级 |
| `promo_revenue_percent` | `promoRevenuePercent` | Q14 促销占比 |
| `elapsed_ms` | `elapsedMs` | 接口耗时 |
| `row_count` | `rowCount` | 返回行数 |
| `task_id` | `taskId` | 导入任务 ID |
| `table_name` | `tableName` | 导入目标表 |
| `file_name` | `fileName` | 文件名 |
| `total_rows` | `totalRows` | 总行数 |
| `success_rows` | `successRows` | 成功行数 |
| `failed_rows` | `failedRows` | 失败行数 |
| `started_at` | `startedAt` | 开始时间 |
| `ended_at` | `endedAt` | 结束时间 |
| `line_number` | `lineNumber` | 错误行号 |
| `field_name` | `fieldName` | 错误字段名 |
| `field_value` | `fieldValue` | 错误原始值 |
| `error_reason` | `errorReason` | 错误原因 |
| `transaction_id` | `transactionId` | 事务 ID |
| `transaction_type` | `transactionType` | 事务类型 |

---

## 2. TPC-H 查询接口 SQL 主体

### 2.1 Q1 定价汇总（GET /api/tpch/q1）

**参数**：`shipDate`（`date`，例如 `'1998-09-01'`）

**Mapper SQL（直接复制）**：

```sql
SELECT
    l_returnflag    AS return_flag,
    l_linestatus    AS line_status,
    SUM(l_quantity) AS sum_quantity,
    SUM(l_extendedprice) AS sum_base_price,
    SUM(l_extendedprice * (1 - l_discount)) AS sum_discounted_price,
    SUM(l_extendedprice * (1 - l_discount) * (1 + l_tax)) AS sum_charge,
    AVG(l_quantity) AS avg_quantity,
    AVG(l_extendedprice) AS avg_price,
    AVG(l_discount) AS avg_disc,
    COUNT(*) AS count_order
FROM lineitem
WHERE l_shipdate <= #{shipDate}
GROUP BY l_returnflag, l_linestatus
ORDER BY l_returnflag, l_linestatus
```

**DTO 示例**：

```java
public class TpchQ1Record {
    private String returnFlag;
    private String lineStatus;
    private BigDecimal sumQuantity;
    private BigDecimal sumBasePrice;
    private BigDecimal sumDiscountedPrice;
    private BigDecimal sumCharge;
    private BigDecimal avgQuantity;
    private BigDecimal avgPrice;
    private BigDecimal avgDisc;
    private Long countOrder;
}
```

**chartData 构造逻辑**（供 C 渲染柱状图）：

```text
xAxis:  records 中的 returnFlag + lineStatus 拼接，如 "A-F", "N-O"
series: sumCharge 数组
```

---

### 2.2 Q5 本地供应商收入（GET /api/tpch/q5）

**参数**：`regionName`（`varchar`，例如 `'ASIA'`）、`startDate`（`date`）、`endDate`（`date`）

**Mapper SQL（直接复制）**：

```sql
SELECT
    TRIM(n.n_name) AS nation_name,
    SUM(l.l_extendedprice * (1 - l.l_discount)) AS revenue
FROM customer c
JOIN orders o
    ON c.c_custkey = o.o_custkey
JOIN lineitem l
    ON l.l_orderkey = o.o_orderkey
JOIN supplier s
    ON s.s_suppkey = l.l_suppkey
JOIN nation n
    ON n.n_nationkey = c.c_nationkey
   AND n.n_nationkey = s.s_nationkey
JOIN region r
    ON r.r_regionkey = n.n_regionkey
WHERE r.r_name = #{regionName}
  AND o.o_orderdate >= #{startDate}
  AND o.o_orderdate < #{endDate}
GROUP BY n.n_name
ORDER BY revenue DESC
```

**DTO 示例**：

```java
public class TpchQ5Record {
    private String nationName;
    private BigDecimal revenue;
}
```

**chartData 构造逻辑**：

```text
xAxis:  nationName 数组
series: revenue 数组
```

---

### 2.3 Q12 运送方式（GET /api/tpch/q12）

**参数**：`shipMode1`（`varchar`）、`shipMode2`（`varchar`）、`startDate`（`date`）、`endDate`（`date`）

**Mapper SQL（直接复制）**：

```sql
SELECT
    TRIM(l.l_shipmode) AS ship_mode,
    SUM(CASE
        WHEN o.o_orderpriority IN ('1-URGENT', '2-HIGH') THEN 1 ELSE 0
    END) AS high_line_count,
    SUM(CASE
        WHEN o.o_orderpriority NOT IN ('1-URGENT', '2-HIGH') THEN 1 ELSE 0
    END) AS low_line_count
FROM orders o
JOIN lineitem l
    ON o.o_orderkey = l.l_orderkey
WHERE l.l_shipmode IN (#{shipMode1}, #{shipMode2})
  AND l.l_commitdate < l.l_receiptdate
  AND l.l_shipdate   < l.l_commitdate
  AND l.l_receiptdate >= #{startDate}
  AND l.l_receiptdate <  #{endDate}
GROUP BY TRIM(l.l_shipmode)
ORDER BY TRIM(l.l_shipmode)
```

**DTO 示例**：

```java
public class TpchQ12Record {
    private String shipMode;
    private Long highLineCount;
    private Long lowLineCount;
}
```

**chartData 构造逻辑**：

```text
xAxis:   shipMode 数组
series1: highLineCount 数组（堆叠柱）
series2: lowLineCount  数组（堆叠柱）
```

---

### 2.4 Q14 促销效果（GET /api/tpch/q14）

**参数**：`month`（`date`，传月份首日，例如 `'1995-09-01'`）

**Mapper SQL（直接复制）**：

```sql
SELECT
    100.00 * SUM(CASE
        WHEN p.p_type LIKE 'PROMO%' THEN l.l_extendedprice * (1 - l.l_discount)
        ELSE 0
    END) / NULLIF(SUM(l.l_extendedprice * (1 - l.l_discount)), 0)
    AS promo_revenue_percent
FROM lineitem l
JOIN part p
    ON p.p_partkey = l.l_partkey
WHERE l.l_shipdate >= #{month}
  AND l.l_shipdate < (#{month}::date + INTERVAL '1 month')::date
```

> **注意**：PostgreSQL `INTERVAL` 写法，JDBC 传参时 `month` 需为 `java.sql.Date` 或 `LocalDate`。

**DTO 示例**：

```java
public class TpchQ14Result {
    private BigDecimal promoRevenuePercent;
}
```

---

## 3. TPC-C 事务 SQL 流程

TPC-C 实现口径：

```text
本项目冻结为课程最小 TPC-C 实现，不是完整 BenchmarkSQL 兼容实现。
stock 表没有 s_dist_01~s_dist_10。
New-Order 插入 order_line.ol_dist_info 时，B 在 Service/Mapper 中传入固定值（如 dist-info-01）或按 districtId 生成。
前端请求不传 olDistInfo。
```

### 3.1 New-Order 事务（POST /api/tpcc/new-order）

**请求字段**：

```json
{
  "warehouseId": 1,
  "districtId":  1,
  "customerId":  1,
  "items": [
    { "itemId": 1001, "quantity": 2 }
  ]
}
```

**Spring 事务框架（按步骤执行）**：

```java
@Transactional(rollbackFor = Exception.class)
public NewOrderResult newOrder(NewOrderRequest req) {
    long start = System.currentTimeMillis();
    String txId = UUID.randomUUID().toString();

    try {
        // 步骤0：设置事务上下文（供触发器写 stock_change_log）
        jdbcTemplate.execute(
            "SELECT set_config('app.transaction_id', '" + txId + "', true)");
        jdbcTemplate.execute(
            "SELECT set_config('app.change_type', 'new_order', true)");

        // 步骤1：读 warehouse / district / customer 税率
        //   SQL → tpcc_no_get_context(warehouseId, districtId, customerId)
        //   返回：warehouseTax, districtTax, customerDiscount, nextOrderId

        // 步骤2：原子递增 district.d_next_o_id，获取新 orderId
        //   SQL → tpcc_no_increment_next_order_id(warehouseId, districtId)
        //   返回：orderId

        // 步骤3：插入 tpcc_orders 头
        //   SQL → tpcc_no_insert_order(warehouseId, districtId, orderId, customerId, itemCount, 1)

        // 步骤4：插入 new_order 标记
        //   SQL → tpcc_no_insert_new_order(warehouseId, districtId, orderId)

        // 步骤5：循环每个商品
        //   a. 验证商品与库存：tpcc_no_get_item_stock(warehouseId, itemId)
        //   b. 扣减库存：tpcc_no_update_stock(warehouseId, itemId, quantity)
        //      若 RETURNING 返回 0 行 → 库存不足 → 抛出异常触发 rollback
        //   c. 插入明细行：tpcc_no_insert_order_line(..., distInfo)
        //      distInfo 由 B 生成，示例固定为 "dist-info-01"

        // 步骤6：写 transaction_log（committed）
        //   SQL → tpcc_insert_transaction_log(txId, 'new_order', 'committed', elapsedMs, null, userId)

        return buildResult(txId, orderId, elapsedMs);

    } catch (Exception e) {
        // Spring @Transactional 自动 rollback
        // 尽量写 transaction_log（status='failed'），用新连接/新事务
        throw new BusinessException("New-Order failed: " + e.getMessage());
    }
}
```

**各步骤 SQL 主体（复制到 Mapper）**：

```sql
-- 步骤1
SELECT w.w_tax AS warehouse_tax, d.d_tax AS district_tax,
       c.c_discount AS customer_discount, d.d_next_o_id AS next_order_id
FROM warehouse w
JOIN district d ON d.d_w_id = w.w_id
JOIN tpcc_customer c ON c.c_w_id = d.d_w_id AND c.c_d_id = d.d_id
WHERE w.w_id = #{warehouseId} AND d.d_id = #{districtId} AND c.c_id = #{customerId}

-- 步骤2
UPDATE district
SET d_next_o_id = d_next_o_id + 1
WHERE d_w_id = #{warehouseId} AND d_id = #{districtId}
RETURNING d_next_o_id - 1 AS order_id

-- 步骤3
INSERT INTO tpcc_orders (o_id, o_d_id, o_w_id, o_c_id, o_entry_d, o_carrier_id, o_ol_cnt, o_all_local)
VALUES (#{orderId}, #{districtId}, #{warehouseId}, #{customerId}, NOW(), NULL, #{itemCount}, 1)

-- 步骤4
INSERT INTO new_order (no_o_id, no_d_id, no_w_id)
VALUES (#{orderId}, #{districtId}, #{warehouseId})

-- 步骤5a
SELECT i.i_price AS item_price, s.s_quantity AS stock_quantity
FROM item i JOIN stock s ON s.s_i_id = i.i_id
WHERE s.s_w_id = #{warehouseId} AND i.i_id = #{itemId}

-- 步骤5b（返回0行=库存不足，需抛异常）
UPDATE stock
SET s_quantity = s_quantity - #{quantity}, s_ytd = s_ytd + #{quantity}, s_order_cnt = s_order_cnt + 1
WHERE s_w_id = #{warehouseId} AND s_i_id = #{itemId} AND s_quantity >= #{quantity}
RETURNING s_i_id AS item_id, s_quantity AS new_quantity

-- 步骤5c
INSERT INTO order_line (ol_o_id, ol_d_id, ol_w_id, ol_number, ol_i_id, ol_supply_w_id, ol_delivery_d, ol_quantity, ol_amount, ol_dist_info)
VALUES (#{orderId}, #{districtId}, #{warehouseId}, #{lineNumber}, #{itemId}, #{warehouseId}, NULL, #{quantity}, #{amount}, 'dist-info-01')

-- 步骤6
INSERT INTO transaction_log (transaction_id, transaction_type, status, elapsed_ms, error_message, executed_by)
VALUES (#{txId}, 'new_order', 'committed', #{elapsedMs}, NULL, #{userId})
```

---

### 3.2 Payment 事务（POST /api/tpcc/payment）

**请求字段**：

```json
{
  "warehouseId": 1,
  "districtId":  1,
  "customerId":  1,
  "amount":      25.00
}
```

**各步骤 SQL 主体**：

```sql
-- 步骤1：验证并读余额
SELECT c.c_balance AS current_balance, c.c_ytd_payment AS current_ytd_payment
FROM warehouse w
JOIN district d ON d.d_w_id = w.w_id
JOIN tpcc_customer c ON c.c_w_id = d.d_w_id AND c.c_d_id = d.d_id
WHERE w.w_id = #{warehouseId} AND d.d_id = #{districtId} AND c.c_id = #{customerId}

-- 步骤2：更新仓库 YTD
UPDATE warehouse SET w_ytd = w_ytd + #{amount} WHERE w_id = #{warehouseId}

-- 步骤3：更新区域 YTD
UPDATE district SET d_ytd = d_ytd + #{amount}
WHERE d_w_id = #{warehouseId} AND d_id = #{districtId}

-- 步骤4：更新客户余额（RETURNING 用于响应返回）
UPDATE tpcc_customer
SET c_balance = c_balance - #{amount},
    c_ytd_payment = c_ytd_payment + #{amount},
    c_payment_cnt = c_payment_cnt + 1
WHERE c_w_id = #{warehouseId} AND c_d_id = #{districtId} AND c_id = #{customerId}
RETURNING c_id AS customer_id, c_balance AS new_balance

-- 步骤5：插入历史记录
INSERT INTO history (h_c_id, h_c_d_id, h_c_w_id, h_d_id, h_w_id, h_date, h_amount, h_data)
VALUES (#{customerId}, #{districtId}, #{warehouseId}, #{districtId}, #{warehouseId}, NOW(), #{amount}, 'payment')

-- 步骤6：写事务日志
INSERT INTO transaction_log (transaction_id, transaction_type, status, elapsed_ms, error_message, executed_by)
VALUES (#{txId}, 'payment', 'committed', #{elapsedMs}, NULL, #{userId})
```

---

## 4. 数据导入接口 SQL 模板

### 4.1 合法行批量插入（orders）

```sql
INSERT INTO orders (o_orderkey, o_custkey, o_orderstatus, o_totalprice, o_orderdate,
                    o_orderpriority, o_clerk, o_shippriority, o_comment)
VALUES (#{orderKey}, #{custKey}, #{orderStatus}, #{totalPrice}, #{orderDate},
        #{orderPriority}, #{clerk}, #{shipPriority}, #{comment})
ON CONFLICT (o_orderkey) DO NOTHING
```

### 4.2 合法行批量插入（lineitem）

```sql
INSERT INTO lineitem (l_orderkey, l_partkey, l_suppkey, l_linenumber, l_quantity,
                      l_extendedprice, l_discount, l_tax, l_returnflag, l_linestatus,
                      l_shipdate, l_commitdate, l_receiptdate, l_shipinstruct, l_shipmode, l_comment)
VALUES (#{orderKey}, #{partKey}, #{suppKey}, #{lineNumber}, #{quantity},
        #{extendedPrice}, #{discount}, #{tax}, #{returnFlag}, #{lineStatus},
        #{shipDate}, #{commitDate}, #{receiptDate}, #{shipInstruct}, #{shipMode}, #{comment})
ON CONFLICT (l_orderkey, l_linenumber) DO NOTHING
```

### 4.3 错误行写入

```sql
INSERT INTO import_error_log (task_id, line_number, field_name, field_value, error_reason)
VALUES (#{taskId}, #{lineNumber}, #{fieldName}, #{fieldValue}, #{errorReason})
```

### 4.4 更新导入任务状态

```sql
UPDATE import_task
SET status       = #{status},
    total_rows   = #{totalRows},
    success_rows = #{successRows},
    failed_rows  = #{failedRows},
    ended_at     = NOW()
WHERE task_id = #{taskId}
```

---

## 5. 程序侧校验规则摘要（导入清洗）

B 在读取每行数据后，执行以下程序侧校验，不合法写 `import_error_log`，合法行批量写入：

| 表 | 字段 | 校验逻辑 | 错误原因示例 |
|---|---|---|---|
| `orders` | `o_totalprice` | 必须为数值，`>= 0` | `"金额不能为负数"` |
| `orders` | `o_orderdate` | 合法 `date`，格式 `yyyy-MM-dd` | `"日期格式错误"` |
| `orders` | `o_custkey` | 非空整数 | `"客户编号不能为空"` |
| `orders` | `o_orderstatus` | 必须在 `O/F/P` 中 | `"订单状态非法"` |
| `lineitem` | `l_quantity` | 非空，数值，`> 0` | `"数量必须大于0"` |
| `lineitem` | `l_discount` | 数值，`0 <= x <= 1` | `"折扣范围错误"` |
| `lineitem` | `l_shipdate` | 合法 `date` | `"发货日期格式错误"` |
| `partsupp` | `ps_availqty` | 整数，`>= 0` | `"库存数量不能为负"` |

---

## 6. 测试账号 / 样例参数

> 依赖 `sql/V11__sample_data.sql` 导入后生效。

**测试账号**：

| username | password | role |
|---|---|---|
| `admin` | `admin123` | `admin` |
| `user1` | `user123` | `user` |

**TPC-H 样例参数**：

| 查询 | 参数 | 示例值 |
|---|---|---|
| Q1 | `shipDate` | `1998-09-01` |
| Q5 | `regionName`, `startDate`, `endDate` | `ASIA`, `1994-01-01`, `1995-01-01` |
| Q12 | `shipMode1`, `shipMode2`, `startDate`, `endDate` | `MAIL`, `SHIP`, `1994-01-01`, `1995-01-01` |
| Q14 | `month` | `1995-09-01` |

**TPC-C 样例参数**：

```json
{
  "warehouseId": 1,
  "districtId":  1,
  "customerId":  1,
  "items": [{ "itemId": 1001, "quantity": 2 }]
}
```
