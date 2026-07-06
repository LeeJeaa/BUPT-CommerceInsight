# EXPLAIN 基线分析规范

## 1. 输出位置

```text
report/explain_plans/
```

建议文件：

```text
q1_without_index.txt
q1_with_index.txt
q5_without_index.txt
q5_with_index.txt
q12_without_index.txt
q12_with_index.txt
q14_without_index.txt
q14_with_index.txt
```

## 2. 记录字段

```text
查询名称
数据规模
是否有索引
执行时间
扫描方式
连接方式
排序或聚合节点
主要瓶颈
优化结论
```

## 3. 分析口径

```text
不要只贴计划，要解释计划。
重点观察 Seq Scan、Index Scan、Hash Join、Nested Loop、Sort、Aggregate。
性能结论必须对应具体数据规模和执行时间。
```

