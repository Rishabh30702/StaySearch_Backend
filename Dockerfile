# Use Maven image to build the application
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the project files
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Use OpenJDK for running the application
FROM openjdk:17
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/StaySearchBackend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
