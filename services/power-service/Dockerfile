# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
ARG VERSION=0.0.0-dev

WORKDIR /app

# Copy pom.xml first for dependency layer caching
# NOTE: pom.xml in repo must stay at 0.0.0-dev - version set at build time via ARG
COPY pom.xml ./

# Download dependencies - stored in layer for GHA cache
# This layer is cached as long as pom.xml doesn't change
RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ src/

# Set version and build (deps already available from previous layer)
RUN mvn versions:set -DnewVersion=${VERSION} -DgenerateBackupPoms=false -q && \
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

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
