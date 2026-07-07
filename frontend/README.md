# TPC CommerceInsight Frontend

成员 C 前端工程，基于 Vue 3 + Vite + Element Plus + ECharts。

## 运行

```powershell
npm.cmd install
npm.cmd run dev
```

默认地址：

```text
http://localhost:5173
```

## Mock / 真实接口

默认使用前端 Mock，可独立演示。

切换真实后端：

```powershell
$env:VITE_USE_MOCK="false"
npm.cmd run dev
```

Vite 代理：

```text
/api -> http://localhost:8080
```

## 测试账号

```text
admin / admin123
user1 / user123
```

## 构建

```powershell
npm.cmd run build
```
