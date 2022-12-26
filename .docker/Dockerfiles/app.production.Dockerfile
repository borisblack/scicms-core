# Build
FROM docker-hub.iss-reshetnev.ru/registry/languages/java/712/gradle:alpine AS builder

ARG APP_ROOT

WORKDIR $APP_ROOT
COPY . .

USER root

RUN mkdir /app/.gradle
RUN chown -R gradle /app

USER gradle
RUN ./gradlew build

# Deploy
FROM docker-hub-proxy.iss-reshetnev.ru/openjdk:11

WORKDIR /app

COPY --from=builder /app/build/libs/scicms-core-0.1.0-SNAPSHOT.jar scicms-core-0.1.0-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "scicms-core-0.1.0-SNAPSHOT.jar"]
