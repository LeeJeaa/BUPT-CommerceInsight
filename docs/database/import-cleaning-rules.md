# 导入清洗规则

## 1. 适用范围

本文档用于系统演示导入。正式 TPC-H 数据初始化由 D 使用 dbgen + COPY 完成。

## 2. 通用规则

```text
必填字段不能为空。
整数、金额、日期必须能解析。
金额不能为负。
折扣字段范围为 0 到 1。
日期字段必须符合 yyyy-MM-dd。
主键冲突按导入任务配置处理，默认记入错误日志并跳过。
```

## 3. 表级规则

| 表 | 字段 | 规则 |
|---|---|---|
| `orders` | `o_orderkey` | 非空整数 |
| `orders` | `o_custkey` | 非空整数 |
| `orders` | `o_totalprice` | 数值，且大于等于 0 |
| `orders` | `o_orderdate` | 合法日期 |
| `lineitem` | `l_orderkey` | 非空整数 |
| `lineitem` | `l_quantity` | 数值，且大于 0 |
| `lineitem` | `l_discount` | 数值，范围 0 到 1 |
| `partsupp` | `ps_availqty` | 整数，且大于等于 0 |

## 4. 错误日志字段

```text
task_id
line_no
field_name
raw_value
reason
created_at
```

## 5. 前后端展示

B 返回 `totalRows`、`successRows`、`failedRows`、`elapsedMs`；C 展示进度、成功数、失败数和错误明细。

