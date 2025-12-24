# Spring Boot JDK 21 + MyBatis + TkMapper + Vite + Element Plus

This repository provides a runnable starter with:
- Spring Boot 3 (JDK 21)
- MyBatis + TkMapper
- H2 in-memory database with sample data
- Vite + Vue 3 + Element Plus frontend

## Backend

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api/users`.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

The UI will be available at `http://localhost:5173` and proxies `/api` to the backend.

## Deployment (Render Free)

This repo includes a Render setup that builds the Vite frontend and packages it into the Spring Boot JAR.

1. Create a new **Web Service** on Render and connect this repository.
2. Ensure Render detects `render.yaml` and uses it.
3. Add a deploy hook in Render and copy the URL.
4. In GitHub, set the repository secret `RENDER_DEPLOY_HOOK` with the deploy hook URL.

On every push to `main` (or via manual dispatch), GitHub Actions triggers the Render deploy hook to publish the latest version.

