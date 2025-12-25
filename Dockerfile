FROM ghcr.io/bitnami/node:20 AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM ghcr.io/bitnami/maven:3.9.8 AS backend-build
WORKDIR /app
ENV JAVA_TOOL_OPTIONS="-XX:UseSVE=0"
COPY pom.xml ./
COPY src ./src
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn -DskipTests package

FROM ghcr.io/bitnami/java:21 AS runtime
WORKDIR /app
COPY --from=backend-build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
