FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/demo-checkout-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
