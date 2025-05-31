# Stage 1: Build the application
FROM gradle:8.10-jdk21 AS builder

# Set working directory
WORKDIR /app

# Copy the entire project
COPY ./ .

# Build only the desired module's bootJar
RUN ./gradlew :account-service:bootJar

# Stage 2: Run the application
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=builder /app/account-service/build/libs/*.jar app.jar

# Run the Spring Boot app
EXPOSE 8081
ENTRYPOINT ["java", "-Xmx1G", "-jar", "app.jar"]
