# Use Maven image to build the application
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the project files
COPY . .

# Install PaytmChecksum.jar into local maven repo inside container
RUN mvn install:install-file -Dfile=libs/PaytmChecksum.jar -DgroupId=com.paytm -DartifactId=paytm-checksum -Dversion=1.0.0 -Dpackaging=jar

# Now build the project with the dependency available
RUN mvn clean package -DskipTests

# FIX: Use Eclipse Temurin for running the application
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/StaySearchBackend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port (Render usually listens on 8080 or 10000)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]