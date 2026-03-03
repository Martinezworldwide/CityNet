# Multi-stage Dockerfile for CityNet Spring Boot backend
# Stage 1: build the application with Maven
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Copy Maven descriptor and sources
COPY pom.xml .
COPY src ./src

# Build the jar without running tests
RUN mvn -B -DskipTests package

# Stage 2: run the application on a slim JRE image
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /workspace/target/citynet-backend-0.0.1-SNAPSHOT.jar /app/app.jar

# The backend listens on port 8886 by default; Render will inject PORT.
ENV PORT=8886
EXPOSE 8886

# Start the Spring Boot application; server.port is configured as ${PORT:8886}
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

