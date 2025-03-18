# Use OpenJDK 17 as base image
FROM openjdk:17 AS build

# Set the working directory for the build
WORKDIR /app

# Copy the Maven project files
COPY . .

# Build the application (Maven will create the JAR in the target folder)
RUN ./mvnw clean package -DskipTests

# Use a new, smaller image for running the JAR
FROM openjdk:17

# Set the working directory for the final container
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/StaySearchBackend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
