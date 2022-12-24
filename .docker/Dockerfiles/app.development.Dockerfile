FROM docker-hub.iss-reshetnev.ru/registry/languages/java/712/gradle:alpine

WORKDIR /app
COPY . .

USER root

RUN mkdir /app/.gradle
RUN chown -R gradle /app

USER gradle
#RUN gradle build -x test


#ENTRYPOINT ["java", "-jar", "build/libs/scicms-core-0.1.0-SNAPSHOT.jar"]
ENTRYPOINT ["sleep", "9000000"]


