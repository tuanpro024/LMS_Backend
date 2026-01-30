# Stage 1: Build
FROM maven:3.9.8-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent and common modules
COPY repo-common-parent ./repo-common-parent
COPY repo-common-framework ./repo-common-framework

# Install common modules to local repository
RUN mvn -f repo-common-parent/pom.xml clean install -DskipTests
RUN mvn -f repo-common-framework/pom.xml clean install -DskipTests

# Copy and install content common module
COPY repo-content-common ./repo-content-common
RUN mvn -f repo-content-common/pom.xml clean install -DskipTests

# Copy and build learning-path
COPY repo-learning-path ./repo-learning-path
RUN mvn -f repo-learning-path/pom.xml clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/repo-learning-path/target/repo-learning-path-*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
