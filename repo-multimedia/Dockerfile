FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the pre-built jar file
# This assumes you have already run `mvn clean package` or similar
COPY target/*.jar app.jar

EXPOSE 8091

ENTRYPOINT ["java", "-jar", "app.jar"]
