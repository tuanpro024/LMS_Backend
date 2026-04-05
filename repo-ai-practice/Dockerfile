# ── Stage 1: Build ──────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper + pom first (layer cache for deps)
COPY .mvn .mvn
COPY mvnw.cmd mvnw
COPY pom.xml .

# Download deps (cached unless pom.xml changes)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q || true

# Copy source + build
COPY src src
RUN ./mvnw package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# JWT public key directory
RUN mkdir -p secrets

COPY --from=builder /app/target/repo-ai-practice-*.jar app.jar

EXPOSE 8092

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
