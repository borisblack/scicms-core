# Build
FROM docker-hub.iss-reshetnev.ru/registry/languages/java/712/gradle:alpine AS builder

ARG APP_ROOT

WORKDIR $APP_ROOT
COPY . .

USER root

RUN mkdir /app/.gradle
RUN chown -R gradle /app

USER gradle
RUN ./gradlew --build-file build-iss.gradle.kts clean build -x test

# Deploy
FROM docker-hub-proxy.iss-reshetnev.ru/openjdk:11

WORKDIR /app

COPY --from=builder /app/build/libs/scicms-core-0.1.0-SNAPSHOT.jar build/libs/scicms-core-0.1.0-SNAPSHOT.jar
COPY --from=builder /app/build/resources/main/schema build/resources/main/schema

ENTRYPOINT ["java", "-jar", "build/libs/scicms-core-0.1.0-SNAPSHOT.jar"]
