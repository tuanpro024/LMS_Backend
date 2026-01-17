FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/repo-writing-1.0.0.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
