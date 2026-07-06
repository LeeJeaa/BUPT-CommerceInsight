# TPC-C 事务设计规范

## 1. 通用事务要求

```text
使用 Spring 事务管理。
成功 commit。
异常 rollback。
记录 transaction_log。
New-Order 库存变化记录 stock_change_log。
返回 transactionId/status/elapsedMs。
```

## 2. New-Order

涉及表：

```text
warehouse
district
tpcc_customer
tpcc_orders
new_order
order_line
item
stock
stock_change_log
transaction_log
```

流程：

```text
1. 校验 warehouse/district/customer。
2. 获取并递增 district 下一订单号。
3. 插入 tpcc_orders。
4. 插入 new_order。
5. 对每个商品校验 item 和 stock。
6. 扣减 stock 数量。
7. 插入 order_line。
8. 写 stock_change_log。
9. 写 transaction_log。
10. commit；任一步失败 rollback。
```

## 3. Payment

涉及表：

```text
warehouse
district
tpcc_customer
history
transaction_log
```

流程：

```text
1. 校验 warehouse/district/customer。
2. 更新 warehouse 收款金额。
3. 更新 district 收款金额。
4. 更新 customer 余额和支付次数。
5. 插入 history。
6. 写 transaction_log。
7. commit；任一步失败 rollback。
```

