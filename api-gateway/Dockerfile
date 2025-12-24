FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app

COPY gradle.properties .
COPY settings.gradle .
COPY build.gradle .

COPY api-gateway api-gateway

RUN gradle :api-gateway:build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/api-gateway/build/libs/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:49999/actuator/health || exit 1

EXPOSE 49999

ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
