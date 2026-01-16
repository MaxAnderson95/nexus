# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
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
COPY --from=build /app/target/*.jar app.jar

# Run as non-root user
RUN groupadd -g 1001 appgroup && useradd -u 1001 -g appgroup appuser
USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
