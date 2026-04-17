FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/platform-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
