FROM adoptopenjdk/openjdk11-openj9:alpine-slim

WORKDIR /companion

COPY target/*-jar-with-dependencies.jar ./mango-companion.jar

CMD ["java", "-jar", "mango-companion.jar", "/library"]