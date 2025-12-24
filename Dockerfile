ARG SERVICE_NAME
ARG GRADLE_VERSION=8.10.2-jdk21

FROM gradle:${GRADLE_VERSION} AS deps

WORKDIR /app

COPY gradle.properties settings.gradle build.gradle ./

ENV GRADLE_OPTS="-Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.caching=true \
                 -Dorg.gradle.configureondemand=true \
                 -Dhttps.protocols=TLSv1.2,TLSv1.3 \
                 -Xmx2g -Xms512m"

RUN --mount=type=cache,target=/root/.gradle/caches,sharing=locked \
    --mount=type=cache,target=/root/.gradle/wrapper,sharing=locked \
    gradle dependencies --no-daemon --parallel --quiet || true

FROM gradle:${GRADLE_VERSION} AS build

ARG SERVICE_NAME
WORKDIR /app

COPY --from=deps /root/.gradle /root/.gradle

COPY gradle.properties settings.gradle build.gradle ./

COPY eureka-server ./eureka-server
COPY config-server ./config-server
COPY api-gateway ./api-gateway
COPY order-service ./order-service
COPY kitchen-service ./kitchen-service
COPY menu-service ./menu-service
COPY inventory-service ./inventory-service
COPY billing-service ./billing-service

ENV GRADLE_OPTS="-Dorg.gradle.daemon=false \
                 -Dorg.gradle.parallel=true \
                 -Dorg.gradle.caching=true \
                 -Dhttps.protocols=TLSv1.2,TLSv1.3 \
                 -Xmx2g"

RUN --mount=type=cache,target=/root/.gradle/caches,sharing=locked \
    --mount=type=cache,target=/root/.gradle/wrapper,sharing=locked \
    gradle :${SERVICE_NAME}:build -x test --no-daemon --parallel --quiet

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
