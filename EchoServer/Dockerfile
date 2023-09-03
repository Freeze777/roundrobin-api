FROM openjdk:21-jdk

# Set the working directory inside the container
WORKDIR /app
EXPOSE 8080

# Copy the packaged JAR file into the container at the defined working directory
COPY target/SimpleServer-0.0.1-SNAPSHOT.jar /app/echo-server.jar

# Specify the command to run your Spring Boot application
ENTRYPOINT ["java", "-jar", "echo-server.jar"]
