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
