# Start from a base image with JDK 17 (or your Java version)
FROM openjdk:17-jdk-alpine

# Set working directory inside the container
WORKDIR /app

# Copy the built jar from your local machine into the container
COPY target/arbnet-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the port your Spring Boot app runs on (default 8080)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]