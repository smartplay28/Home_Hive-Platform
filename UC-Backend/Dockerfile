# ──────────────────────────────────────────────────────────────────────────────
# HomeHive Spring Boot Backend — Dockerfile
# Multi-stage build: Maven build → lightweight JRE runtime image
# ──────────────────────────────────────────────────────────────────────────────

# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom first (Docker cache layer for deps)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copy source and build (skip tests — run separately in CI)
COPY src src
RUN ./mvnw package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Security: run as non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Pass env vars at runtime:
#   docker run -e MONGO_URI=... -e JWT_SECRET=... -e REDIS_HOST=... homehive-backend
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
