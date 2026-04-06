# Stage 1: Build
FROM maven:3.9.8-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy shared modules first so Maven can resolve local parent/framework dependencies.
COPY repo-common-parent ./repo-common-parent
COPY repo-common-framework ./repo-common-framework
COPY repo-content-common ./repo-content-common

# Install shared modules into the local Maven repository.
RUN mvn -f repo-common-parent/pom.xml clean install -DskipTests
RUN mvn -f repo-common-framework/pom.xml clean install -DskipTests
RUN mvn -f repo-content-common/pom.xml clean install -DskipTests

# Copy and build AI practice service.
COPY repo-ai-practice ./repo-ai-practice
RUN mvn -f repo-ai-practice/pom.xml clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/repo-ai-practice/target/repo-ai-practice-*.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]
