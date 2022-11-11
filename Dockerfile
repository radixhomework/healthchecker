FROM azul/zulu-openjdk-alpine:17.0.3-17.34.19-x86

RUN mkdir /config /log /app
VOLUME /config
VOLUME /log

COPY target/healthchecker.jar /app/healthchecker.jar

ENTRYPOINT java \
    -jar /app/healthchecker.jar \
    --spring.config.location=/config/application.yml \
    --logging.config=/config/logback.xml
