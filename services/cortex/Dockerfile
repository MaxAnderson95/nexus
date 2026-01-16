# Frontend build stage
FROM node:20-alpine AS frontend-build
ARG VERSION=0.0.0-dev

WORKDIR /frontend

# Copy package files
COPY frontend/package*.json ./

# Install dependencies (cached via BuildKit mount)
RUN --mount=type=cache,target=/root/.npm \
    npm install

# Copy frontend source
COPY frontend/ .

# Set version and build the frontend
RUN npm version ${VERSION} --no-git-tag-version --allow-same-version && \
    npm run build

# Java build stage
FROM maven:3.9-eclipse-temurin-17 AS java-build
ARG VERSION=0.0.0-dev

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml ./

# Download dependencies (cached via BuildKit mount)
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application with version override (reuse cached dependencies)
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn versions:set -DnewVersion=${VERSION} -DgenerateBackupPoms=false -q && \
    mvn package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Copy the built jar
COPY --from=java-build /app/target/*.jar app.jar

# Copy frontend static files to serve from Spring Boot
COPY --from=frontend-build /frontend/dist/ /app/static/

# Run as non-root user
RUN groupadd -g 1001 appgroup && useradd -u 1001 -g appgroup appuser
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
