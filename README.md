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

## Docker Auto Deploy (GitHub Container Registry)

This repo includes a GitHub Actions workflow that builds and pushes the Docker image to
GitHub Container Registry (GHCR).

Publish triggers:
- Push to `main` (publishes `ghcr.io/<owner>/springboot-vite-element:main`)
- Publish a GitHub Release or push a tag like `v1.0.0` (publishes version tags)

Pull and run the published image:

```bash
docker pull ghcr.io/<owner>/springboot-vite-element:<tag>
docker run --rm -p 8080:8080 ghcr.io/<owner>/springboot-vite-element:<tag>
```
