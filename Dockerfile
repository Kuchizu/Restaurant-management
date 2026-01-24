ARG SERVICE_NAME
ARG GRADLE_VERSION=8.10.2-jdk21

FROM gradle:${GRADLE_VERSION} AS build

ARG SERVICE_NAME
WORKDIR /app

COPY gradle.properties build.gradle ./

# Copy common-events module (shared dependency)
COPY common-events ./common-events

COPY ${SERVICE_NAME} ./${SERVICE_NAME}

# Create minimal settings.gradle for Docker build
RUN echo "rootProject.name = 'restaurant-microservices'" > settings.gradle && \
    echo "include 'common-events'" >> settings.gradle && \
    echo "include '${SERVICE_NAME}'" >> settings.gradle

ENV GRADLE_OPTS="-Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.caching=true \
                 -Dorg.gradle.configureondemand=true \
                 -Dorg.gradle.vfs.watch=false \
                 -Dhttps.protocols=TLSv1.2,TLSv1.3 \
                 -Xmx2g"

RUN --mount=type=cache,target=/root/.gradle/caches,sharing=locked \
    --mount=type=cache,target=/root/.gradle/wrapper,sharing=locked \
    --mount=type=cache,target=/app/.gradle,sharing=locked \
    gradle :${SERVICE_NAME}:build -x test --no-daemon --build-cache

FROM eclipse-temurin:21-jre-alpine AS runtime

ARG SERVICE_NAME
WORKDIR /app

RUN apk add --no-cache curl wget ca-certificates tzdata && \
    addgroup -S spring && adduser -S spring -G spring

ENV TZ=Europe/Moscow

COPY --from=build --chown=spring:spring /app/${SERVICE_NAME}/build/libs/*.jar app.jar

USER spring:spring

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=100 \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.backgroundpreinitializer.ignore=true"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
