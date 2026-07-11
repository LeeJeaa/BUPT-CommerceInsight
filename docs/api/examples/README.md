# API 示例

这些示例默认后端运行在 `http://localhost:8080`，测试账号统一为：

```text
admin / admin123
user1 / user123
```

真实后端联调必须显式使用 `dev` 或 `prod` profile；仅前端独立演示才使用 `mock` profile。

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Windows PowerShell 可把 `curl` 换成 `curl.exe`。

先调用 `auth-login.curl`，将响应中的 `data.token` 保存为环境变量 `TOKEN`；其余真实 profile 示例均使用 `Authorization: Bearer $TOKEN`。固定 `mock-token` 只适用于 `mock` profile。
