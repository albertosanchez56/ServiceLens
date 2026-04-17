FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# JAR ejecutable de Spring Boot (no usar *-plain.jar; el wildcard podía copiar el JAR sin manifest)
COPY target/demo-edge-api-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
