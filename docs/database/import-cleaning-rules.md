# 导入清洗规则

> 适用范围：系统演示导入（B 的 API 接口层校验）  
> 不适用：D 的 dbgen + COPY 正式初始化导入  
> SQL 资产位置：`sql/V7__import_cleaning_rules.sql`

---

## 1. 通用原则

```text
1. 所有校验在程序侧（Java）完成，不依赖数据库约束拦截。
2. 每行独立校验，一行出错不影响其他行。
3. 合法行批量写入目标表。
4. 非法行写入 import_error_log，继续处理下一行。
5. import_task 记录 total_rows、success_rows、failed_rows 和最终状态。
6. 主键冲突默认策略：跳过并记录错误，不覆盖已有数据。
7. 数据库约束要求 success_rows + failed_rows <= total_rows，B 更新任务统计前应先在程序侧校验。
```

---

## 2. 空值检查规则

下列字段在任何情况下不允许为空（NULL 或空字符串）。  
错误原因：`required_field_empty`

### 2.1 orders 表

| 字段 | 说明 |
|---|---|
| `o_orderkey` | 订单主键，不可为空 |
| `o_custkey` | 客户编号，不可为空 |
| `o_orderstatus` | 订单状态，不可为空 |
| `o_totalprice` | 订单金额，不可为空 |
| `o_orderdate` | 订单日期，不可为空 |
| `o_orderpriority` | 订单优先级，不可为空 |
| `o_clerk` | 经办人，不可为空 |
| `o_shippriority` | 发运优先级，不可为空 |

### 2.2 lineitem 表

| 字段 | 说明 |
|---|---|
| `l_orderkey` | 关联订单编号，不可为空 |
| `l_partkey` | 零件编号，不可为空 |
| `l_suppkey` | 供应商编号，不可为空 |
| `l_linenumber` | 明细行号，不可为空 |
| `l_quantity` | 数量，不可为空 |
| `l_extendedprice` | 扩展价格，不可为空 |
| `l_discount` | 折扣，不可为空 |
| `l_tax` | 税率，不可为空 |
| `l_returnflag` | 退货标志，不可为空 |
| `l_linestatus` | 明细状态，不可为空 |
| `l_shipdate` | 发货日期，不可为空 |
| `l_commitdate` | 承诺日期，不可为空 |
| `l_receiptdate` | 收货日期，不可为空 |
| `l_shipmode` | 运输方式，不可为空 |

### 2.3 partsupp 表

| 字段 | 说明 |
|---|---|
| `ps_partkey` | 零件编号，不可为空 |
| `ps_suppkey` | 供应商编号，不可为空 |
| `ps_availqty` | 可用库存，不可为空 |
| `ps_supplycost` | 供应成本，不可为空 |

---

## 3. 类型检查规则

下列字段必须能解析为对应的数据类型，否则写入错误日志。

### 3.1 整数字段

错误原因：`invalid_integer`

| 表 | 字段 | 目标类型 |
|---|---|---|
| `orders` | `o_orderkey` | `integer` |
| `orders` | `o_custkey` | `integer` |
| `orders` | `o_shippriority` | `integer` |
| `lineitem` | `l_orderkey` | `integer` |
| `lineitem` | `l_partkey` | `integer` |
| `lineitem` | `l_suppkey` | `integer` |
| `lineitem` | `l_linenumber` | `integer` |
| `partsupp` | `ps_partkey` | `integer` |
| `partsupp` | `ps_suppkey` | `integer` |
| `partsupp` | `ps_availqty` | `integer` |

Java 校验方式：
```java
try {
    Integer.parseInt(rawValue);
} catch (NumberFormatException e) {
    // 写 import_error_log，error_reason = 'invalid_integer'
}
```

### 3.2 数值字段（Numeric / Decimal）

错误原因：`invalid_numeric`

| 表 | 字段 | 目标类型 |
|---|---|---|
| `orders` | `o_totalprice` | `numeric(15,2)` |
| `lineitem` | `l_quantity` | `numeric(15,2)` |
| `lineitem` | `l_extendedprice` | `numeric(15,2)` |
| `lineitem` | `l_discount` | `numeric(15,2)` |
| `lineitem` | `l_tax` | `numeric(15,2)` |
| `partsupp` | `ps_supplycost` | `numeric(15,2)` |

Java 校验方式：
```java
try {
    new BigDecimal(rawValue);
} catch (NumberFormatException e) {
    // 写 import_error_log，error_reason = 'invalid_numeric'
}
```

### 3.3 日期字段

错误原因：`invalid_date`

| 表 | 字段 | 目标类型 |
|---|---|---|
| `orders` | `o_orderdate` | `date` |
| `lineitem` | `l_shipdate` | `date` |
| `lineitem` | `l_commitdate` | `date` |
| `lineitem` | `l_receiptdate` | `date` |

Java 校验方式（见第 5 节）。

---

## 4. 范围检查规则

通过类型校验后，再对业务范围进行二次检查。

### 4.1 金额 / 价格字段（不可为负）

错误原因：`negative_amount`

| 表 | 字段 | 规则 |
|---|---|---|
| `orders` | `o_totalprice` | `>= 0` |
| `lineitem` | `l_extendedprice` | `>= 0` |
| `partsupp` | `ps_supplycost` | `>= 0` |

### 4.2 数量字段（必须大于0）

错误原因：`quantity_not_positive`

| 表 | 字段 | 规则 |
|---|---|---|
| `lineitem` | `l_quantity` | `> 0` |

### 4.3 库存字段（不可为负）

错误原因：`negative_amount`（复用）

| 表 | 字段 | 规则 |
|---|---|---|
| `partsupp` | `ps_availqty` | `>= 0` |

### 4.4 折扣 / 税率字段（0 到 1 之间）

错误原因：`discount_out_of_range`

| 表 | 字段 | 规则 |
|---|---|---|
| `lineitem` | `l_discount` | `0 <= x <= 1` |
| `lineitem` | `l_tax` | `0 <= x <= 1` |

### 4.5 枚举字段

错误原因：`invalid_enum_value`

| 表 | 字段 | 允许值 |
|---|---|---|
| `orders` | `o_orderstatus` | `O`, `F`, `P` |

---

## 5. 日期格式检查规则

### 5.1 格式要求

所有日期字段必须符合格式 `yyyy-MM-dd`（ISO 8601 日期格式）。

| 表 | 字段 | 示例合法值 | 示例非法值 |
|---|---|---|---|
| `orders` | `o_orderdate` | `1995-03-15` | `95-3-15`, `1995/03/15`, `March 15 1995` |
| `lineitem` | `l_shipdate` | `1995-04-10` | `1995-4-10`, `10-04-1995` |
| `lineitem` | `l_commitdate` | `1995-05-20` | `20/05/1995` |
| `lineitem` | `l_receiptdate` | `1995-06-01` | `1995.06.01` |

错误原因：`invalid_date`

### 5.2 Java 校验方式

```java
private static final DateTimeFormatter DATE_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MM-dd");

public static boolean isValidDate(String raw) {
    try {
        LocalDate.parse(raw, DATE_FORMATTER);
        return true;
    } catch (DateTimeParseException e) {
        return false;
    }
}
```

### 5.3 日期逻辑约束（lineitem 三日期关系）

lineitem 中三个日期存在逻辑顺序约束：

```text
l_shipdate < l_commitdate ≤ l_receiptdate
```

违反时使用自定义错误原因：`date_order_violation`

Java 校验：
```java
if (!shipDate.isBefore(commitDate) || commitDate.isAfter(receiptDate)) {
    // 错误原因 = 'date_order_violation'
}
```

---

## 6. 外键存在性检查

错误原因：`foreign_key_not_found`

| 来源表 | 字段 | 引用表 | 引用字段 | 检查方式 |
|---|---|---|---|---|
| `orders` | `o_custkey` | `customer` | `c_custkey` | B 查询验证（可选，正式导入不做） |
| `lineitem` | `l_orderkey` | `orders` | `o_orderkey` | B 查询验证（可选） |
| `partsupp` | `ps_partkey` | `part` | `p_partkey` | B 查询验证（可选） |
| `partsupp` | `ps_suppkey` | `supplier` | `s_suppkey` | B 查询验证（可选） |

> 外键检查属于可选项，对于大批量导入可能性能较差。建议 B 实现时提供"严格模式"（检查外键）和"宽松模式"（仅检查格式）两种选项，系统演示时使用严格模式。

---

## 7. 主键冲突处理策略

### 7.1 默认策略：跳过并记录错误

当 INSERT 返回 0 影响行（使用 `ON CONFLICT DO NOTHING`），B 将该行写入 `import_error_log`：

```java
int affected = jdbcTemplate.update(INSERT_ORDERS_SKIP_DUP, params);
if (affected == 0) {
    logError(taskId, lineNumber, "o_orderkey", rawOrderKey, "primary_key_conflict");
    failedRows++;
} else {
    successRows++;
}
```

对应 SQL 模板（见 `sql/V7__import_cleaning_rules.sql`）：
- `import_insert_orders_skip_dup`
- `import_insert_lineitem_skip_dup`
- `import_insert_partsupp_skip_dup`

### 7.2 可选策略：覆盖更新（Upsert）

当系统演示需要展示"重新导入覆盖"功能时，使用 DO UPDATE 模板：
- `import_upsert_orders`
- `import_upsert_partsupp`

> `lineitem` 不提供 upsert 模板，因为明细行的组合主键 `(l_orderkey, l_linenumber)` 覆盖逻辑复杂，演示时不建议使用。

---

## 8. import_task 和 import_error_log 表结构说明

### 8.1 import_task 字段语义

| 字段 | 类型 | 说明 |
|---|---|---|
| `task_id` | `bigserial` | 任务主键，自增 |
| `table_name` | `varchar(50)` | 导入目标表名，如 `orders` |
| `file_name` | `varchar(255)` | 上传文件名 |
| `status` | `varchar(20)` | 任务状态，枚举值见下 |
| `total_rows` | `bigint` | 文件总行数 |
| `success_rows` | `bigint` | 成功写入行数，和 `failed_rows` 之和不能超过 `total_rows` |
| `failed_rows` | `bigint` | 写入失败行数，和 `success_rows` 之和不能超过 `total_rows` |
| `started_at` | `timestamp` | 任务开始时间（触发器自动补全） |
| `ended_at` | `timestamp` | 任务结束时间（触发器自动补全） |
| `created_by` | `bigint` | FK → `app_user.user_id`，操作用户 |

**status 枚举值**：

| 值 | 含义 |
|---|---|
| `pending` | 任务已创建，未开始 |
| `running` | 正在导入 |
| `success` | 全部行处理完成（含有错误行但不影响任务完成） |
| `failed` | 任务因系统异常中断，非预期结束 |

> 注意：`success` 和 `failed` 的区别：有错误行但任务正常结束 → `success`；任务中途崩溃/超时 → `failed`。

**触发器行为（`trg_import_task_audit`）**：

```text
status 变为 running    → 自动设置 started_at = NOW()
status 变为 success/failed → 自动设置 ended_at = NOW()
status 变为 pending    → 清空 started_at 和 ended_at（用于任务重置）
```

### 8.2 import_error_log 字段语义

| 字段 | 类型 | 说明 |
|---|---|---|
| `error_id` | `bigserial` | 错误记录主键，自增 |
| `task_id` | `bigint` | FK → `import_task.task_id` |
| `line_number` | `bigint` | 原始文件中的行号（从 1 起） |
| `field_name` | `varchar(100)` | 出错的字段名，如 `o_totalprice` |
| `field_value` | `text` | 原始原始值（字符串形式，方便展示） |
| `error_reason` | `text` | 错误原因编码，见下表 |
| `created_at` | `timestamp` | 记录时间，默认 `NOW()` |

### 8.3 错误原因编码表

| 错误原因 | 含义 | 触发条件 |
|---|---|---|
| `required_field_empty` | 必填字段为空 | 字段值为 NULL 或空字符串 |
| `invalid_integer` | 整数解析失败 | 无法 `parseInt` |
| `invalid_numeric` | 数值解析失败 | 无法转 `BigDecimal` |
| `invalid_date` | 日期格式错误 | 不符合 `yyyy-MM-dd` |
| `negative_amount` | 金额为负 | 金额/价格 `< 0` |
| `discount_out_of_range` | 折扣/税率越界 | 不在 `[0, 1]` |
| `quantity_not_positive` | 数量为零或负 | `l_quantity <= 0` |
| `date_order_violation` | 日期逻辑顺序错误 | `l_shipdate >= l_commitdate` 等 |
| `foreign_key_not_found` | 外键引用不存在 | 关联主表无对应记录 |
| `primary_key_conflict` | 主键冲突 | `INSERT ... ON CONFLICT` 返回 0 行 |
| `invalid_enum_value` | 枚举值非法 | `o_orderstatus` 不在 `O/F/P` 中 |

---

## 9. B/C/D 边界

```text
A：定义清洗规则、设计 import_task/import_error_log 表结构、提供 SQL 模板。
B：实现文件读取、程序侧校验、批量写入、错误日志接口、任务状态更新。
C：实现导入页面、进度条、错误日志列表展示、结果截图。
D：提供含错误行的测试数据文件，协助验证导入结果；正式数据初始化不使用本流程。
```

## 10. 数据库负例验证 SQL

以下 SQL 用于 A/B/D 在本地验证约束失败路径，执行时应看到数据库报错；不要放入默认初始化脚本。

### 10.1 import_task 行数一致性失败

```sql
INSERT INTO import_task (
    table_name, file_name, status, total_rows, success_rows, failed_rows, created_by
) VALUES (
    'orders', 'bad-count.csv', 'success', 1, 1, 1, 1
);
```

期望失败：`ck_import_task_row_count_consistent`，因为 `success_rows + failed_rows > total_rows`。

### 10.2 orders 枚举失败

```sql
INSERT INTO orders (
    o_orderkey, o_custkey, o_orderstatus, o_totalprice, o_orderdate,
    o_orderpriority, o_clerk, o_shippriority, o_comment
) VALUES (
    900001, 1, 'X', 10.00, DATE '1995-01-01',
    '1-URGENT', 'Clerk#bad', 0, 'invalid status'
);
```

期望失败：`ck_orders_orderstatus_valid`。

### 10.3 lineitem 外键失败

```sql
INSERT INTO lineitem (
    l_orderkey, l_partkey, l_suppkey, l_linenumber, l_quantity,
    l_extendedprice, l_discount, l_tax, l_returnflag, l_linestatus,
    l_shipdate, l_commitdate, l_receiptdate, l_shipinstruct, l_shipmode, l_comment
) VALUES (
    999999, 1, 1, 1, 1.00,
    10.00, 0.00, 0.00, 'N', 'O',
    DATE '1995-01-01', DATE '1995-01-02', DATE '1995-01-03',
    'DELIVER IN PERSON', 'MAIL', 'missing order'
);
```

期望失败：`fk_lineitem_orders`，因为 `orders.o_orderkey = 999999` 不存在。
