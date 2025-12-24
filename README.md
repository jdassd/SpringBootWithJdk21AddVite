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
