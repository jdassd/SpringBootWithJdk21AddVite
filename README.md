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

## Docker 部署

构建镜像（前端 + 后端打到同一个容器中）：

```bash
docker build -t springboot-vite-element .
```

运行容器：

```bash
docker run --rm -p 8080:8080 springboot-vite-element
```

访问 `http://localhost:8080/`（接口在 `/api/users`）。

> 说明：Dockerfile 使用 GHCR 上的基础镜像（`ghcr.io/bitnami/*`），避免拉取 Docker Hub 时
> 可能遇到的认证/网络问题。构建阶段额外设置了 `JAVA_TOOL_OPTIONS="-XX:UseSVE=0"`，用于
> 规避部分 ARM 环境中 JDK 启动时触发 SIGILL 的问题。

## Docker 自动发布（GitHub Container Registry）

仓库内置 GitHub Actions 工作流，会自动构建并推送镜像到 GHCR。

触发方式：
- 推送到 `main`（发布 `ghcr.io/<owner>/springboot-vite-element:main`）
- 发布 GitHub Release 或推送 `v1.0.0` 这类 tag（发布版本 tag）

拉取并运行发布后的镜像：

```bash
docker pull ghcr.io/<owner>/springboot-vite-element:<tag>
docker run --rm -p 8080:8080 ghcr.io/<owner>/springboot-vite-element:<tag>
```
