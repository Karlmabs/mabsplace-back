# Multi-stage Dockerfile that works for both Railway (build from source) and Kubernetes (pre-built JAR)

# Stage 1: Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build

# Copy Maven wrapper and pom.xml first for better caching
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies (cached layer if pom.xml hasn't changed)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built JAR from builder stage
# This works for both Railway (builds from source) and GitHub Actions (also builds in Docker)
COPY --from=builder /build/target/mabsplace-back-0.0.1.jar ./app.jar

# Set environment variable with default value
ENV SPRING_PROFILES_ACTIVE=prod

# Expose port (Railway auto-detects this)
EXPOSE 8080

# Specify the command to run the JAR file with the profile
CMD ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "app.jar"]