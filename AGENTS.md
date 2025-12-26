# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/example/demo` contains the Spring Boot app (controllers, entities, repositories, auth, config, security).
- `src/main/resources` holds `application.yml`, `schema.sql`, and `data.sql`; frontend builds are copied to `src/main/resources/static`.
- `src/test/java/com/example/demo` contains JUnit tests using Spring Boot Test and MockMvc.
- `frontend/` is the Vite + Vue 3 app; `frontend/src` holds UI components and views.
- `scripts/render-build.sh` builds frontend assets and packages the backend for Render.
- `target/` is generated build output.

## Build, Test, and Development Commands
- `mvn spring-boot:run` starts the backend at `http://localhost:8080`.
- `mvn test` runs backend tests.
- `mvn -DskipTests package` builds the backend jar (used in Docker and Render).
- `cd frontend && npm install` installs frontend dependencies.
- `cd frontend && npm run dev` runs the Vite dev server at `http://localhost:5173` with `/api` proxied to the backend.
- `cd frontend && npm run build` creates production assets in `frontend/dist`.
- `docker build -t springboot-vite-element .` builds the combined container image.

## Coding Style & Naming Conventions
- Java: 4-space indentation, package `com.example.demo`, keep REST controllers in `controller`, JPA entities in `entity`, repositories in `repository`.
- Vue: `<script setup>` with the Composition API, 2-space indentation, semicolons in JS, PascalCase component files (e.g., `HomeView.vue`).
- Keep API routes under `/api/...` and use the `X-Auth-Token` header for authenticated endpoints.

## Testing Guidelines
- Backend tests use JUnit 5 + Spring Boot Test + MockMvc. Name tests `*Test.java` under `src/test/java`.
- Run tests with `mvn test`. No frontend test runner is configured yet.

## Commit & Pull Request Guidelines
- Use Conventional Commits (examples in history: `feat: add authentication controller`).
- PRs should include a short description, test commands run, and screenshots for UI changes; link related issues when applicable.

## Configuration & Security Notes
- H2 in-memory DB is configured in `src/main/resources/application.yml`, with schema/data in `schema.sql` and `data.sql`.
- Mail defaults to `localhost:25`; override in `application.yml` or via environment variables for real SMTP.
