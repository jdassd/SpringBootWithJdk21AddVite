# Spring Boot JDK 21 + MyBatis + TkMapper + Vite + Element Plus

本仓库提供一个可运行的示例工程，包含：
- Spring Boot 3（JDK 21）
- MyBatis + TkMapper
- H2 内存数据库及示例数据
- Vite + Vue 3 + Element Plus 前端

## 后端

```bash
mvn spring-boot:run
```

接口地址：`http://localhost:8080/api/users`。

## 前端

```bash
cd frontend
npm install
npm run dev
```

UI 地址：`http://localhost:5173`，并将 `/api` 代理到后端。

## 管理接口鉴权

管理类接口需要在请求头携带 `X-Auth-Token`（登录接口返回的 token）。前端管理后台会在进入前检查登录状态，并在请求头中补充该字段。

## Docker Deployment

Build the image (frontend + backend in one container):

```bash
docker build -t springboot-vite-element .
```

Run the container:

```bash
docker run --rm -p 8080:8080 springboot-vite-element
```

Then open `http://localhost:8080/` (API under `/api/users`).
