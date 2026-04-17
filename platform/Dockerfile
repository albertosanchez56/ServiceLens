FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/platform-*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
