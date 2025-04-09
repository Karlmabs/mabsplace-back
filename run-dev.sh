#!/bin/bash
# Script to run the application with the dev profile

echo "Starting application with dev profile..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
