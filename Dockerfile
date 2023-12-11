# Use the official openJDK base image with Java 17
FROM openjdk:17

# Set the working directory inside the container
WORKDIR /app

# Copy your Spring Boot application JAR file into the container
COPY ./target/mabsplace-back-0.0.1.jar ./app.jar

# Specify the command to run the JAR file
CMD ["java", "-jar", "app.jar"]