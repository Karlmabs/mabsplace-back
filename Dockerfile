# Use Eclipse Temurin (recommended replacement for OpenJDK) with Java 21
FROM eclipse-temurin:21-jre

# Set the working directory inside the container
WORKDIR /app

# Copy your Spring Boot application JAR file into the container
COPY ./target/mabsplace-back-0.0.1.jar ./app.jar

# Set environment variable with default value
ENV SPRING_PROFILES_ACTIVE=prod

# Specify the command to run the JAR file with the profile
CMD ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "app.jar"]