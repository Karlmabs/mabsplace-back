#!/bin/bash
# Script to run the application with the prod profile

echo "Starting application with prod profile..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
