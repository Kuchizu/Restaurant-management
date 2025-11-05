# syntax=docker/dockerfile:1.4

FROM gradle:8.10.2-jdk21 AS build

WORKDIR /app

COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    gradle dependencies --no-daemon || true

COPY src ./src

# Use cache and skip tests
RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    gradle build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

