# Multi-stage build для config-server
# Stage 1: Build
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app

# Копируем только необходимые файлы для сборки
COPY gradle.properties .
COPY settings.gradle .
COPY build.gradle .

# Копируем исходники конкретного сервиса
COPY config-server config-server

# Собираем приложение (используем gradle из образа вместо wrapper)
RUN gradle :config-server:build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Создаём непривилегированного пользователя
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Копируем jar из builder stage
COPY --from=builder /app/config-server/build/libs/*.jar app.jar

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8888/actuator/health || exit 1

# Expose port
EXPOSE 8888

# JVM опции для контейнера
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

# Запуск приложения
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
