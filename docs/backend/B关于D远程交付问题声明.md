# 成员 B 关于 D 远程交付问题的声明

声明人：成员 B  
分支：dev/b-backend-service  
日期：2026-07-07

## 1. 声明边界

根据组内分工，成员 B 负责 Spring Boot 后端、DTO/VO 字段映射、Mock Repository、业务接口、TPC-H 查询接口、TPC-C 事务接口、统一响应和异常处理。

成员 B 不负责 Docker、dbgen、Python 压测脚本、D 的性能报告文件，也不负责替 D 或集成分支处理合并冲突。

因此，本声明只记录远程 D 交付中发现的风险点，供组长和对应责任人处理。B 分支不直接修改这些 D 范围文件。

## 2. 远程合并冲突风险

检查 `dev/b-backend-service` 与 `origin/develop` 的三方合并结果时发现：

- `.gitignore` 存在双方修改冲突。
- B 侧 `.gitignore` 已覆盖 B 后端需要忽略的内容，包括 `.env`、`.DS_Store`、`backend/.mvn/apache-maven-*`、`backend/.mvn/*.tar.gz`、`backend/target/` 等。
- 如直接采用 D 侧版本，可能丢失 B 后端本地运行产生的忽略规则。

处理建议：

- 由组长或负责集成的成员合并双方 `.gitignore` 规则。
- 不建议简单覆盖任意一方。
- B 当前不在本分支解决该冲突。

## 3. D 交付中的字段契约不一致

远程 `origin/develop` 中 D 相关性能结果文件使用了与 API 契约不一致的字段：

- `report/performance/performance_mock.json` 使用 `successRequests`、`failedRequests`。
- `report/performance/performance_mock.csv` 使用 `successRequests`、`failedRequests`。
- `report/performance/README.md` 中示例字段使用 `successRequests`、`failedRequests`。
- `test/performance_test.py` 中保存性能结果时使用 `successRequests`、`failedRequests`。

但冻结后的后端/API 契约要求使用：

- `successCount`
- `failCount`
- `throughput`

风险：

- C 若参考 D 的 mock JSON/CSV，会得到与 B 后端 API 不一致的字段。
- D 的 Python 压测结果若直接作为 API 示例，会误导后续联调和报告验收。

处理建议：

- D 应按 `docs/api/api-contract.md`、`docs/api/mock-contract.md`、`docs/api/performance-test-endpoints.md` 统一字段。
- C 应以 B 后端 API 和 API 契约为准，不以 D 的 CSV/JSON 字段为准。

## 4. D 文档中的数据库连接说明不一致

远程 `origin/develop` 中 `db/README.md` 存在示例命令使用旧连接信息的问题：

```bash
psql -h localhost -p 5432 -U commerce -d commerce_insight -f db/scripts/count-tables.sql
```

但当前数据库基线口径应为：

- 数据库：`tpc_commerce`
- 用户：`tpc_admin`
- 密码：`tpc_password`

风险：

- 按 D 文档执行可能连接失败。
- 后端 `dev` profile 已按 A/D 基线环境使用 `tpc_commerce` 和 `tpc_admin`。

处理建议：

- D 或集成责任人更新 `db/README.md` 中旧账号、旧库名示例。

## 5. D 状态报告存在过期阻塞项

远程 `origin/develop` 中 `report/final/d-day1-day3-status.md` 仍记录：

- A 尚未交付 SQL。
- B 尚未交付后端接口。

该描述已过期。当前：

- A 已在 `dev/a-database-sql` 和 `origin/develop` 中交付数据库基线内容。
- B 已在 `dev/b-backend-service` 中完成后端接口、Mock、JDBC/MyBatis、TPC-H 和 TPC-C 事务交付。

处理建议：

- D 更新状态报告，避免最终报告中保留过期阻塞项。

## 6. B 自查结论

B 当前分支保持以下状态：

- 不修改 D 的 `db/`、`test/`、`report/performance/` 文件。
- 不替 D 解决 `.gitignore` 合并冲突。
- B 后端 API 字段以冻结契约为准。
- B 提供 `docs/backend/B成员最终交付报告.md` 作为本分支审核依据。

本声明作为 PR 审核备注，不等同于 B 对 D 范围问题的修复承诺。
