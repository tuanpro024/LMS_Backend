# Stage 1: Build
FROM maven:3.9.8-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent and common modules
COPY repo-common-parent ./repo-common-parent
COPY repo-common-framework ./repo-common-framework

# Install common modules to local repository
RUN mvn -f repo-common-parent/pom.xml clean install -DskipTests
RUN mvn -f repo-common-framework/pom.xml clean install -DskipTests

# Copy and build eureka-server
COPY repo-eureka ./repo-eureka
RUN mvn -f repo-eureka/pom.xml clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/repo-eureka/target/repo-eureka-*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
