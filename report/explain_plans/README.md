# EXPLAIN ANALYZE Templates

Save formal query plans in this directory.

Required files after A provides SQL and D runs experiments:

- `q1_without_index.txt`
- `q1_with_index.txt`
- `q5_without_index.txt`
- `q5_with_index.txt`
- `q12_without_index.txt`
- `q12_with_index.txt`
- `q14_without_index.txt`
- `q14_with_index.txt`

Each file should record:

```text
Query:
Scale factor:
Index state:
Execution time:
Scan nodes:
Join nodes:
Sort/Aggregate nodes:
Main bottleneck:
Conclusion:

Raw EXPLAIN ANALYZE:
```
