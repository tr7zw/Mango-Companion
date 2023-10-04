FROM openjdk:17.0-slim

WORKDIR /workspace

COPY target/mango-companion-*-jar-with-dependencies.jar /workspace/mango-companion.jar
COPY webapp/style.css ./webapp/style.css
COPY webapp/WEB-INF/web.xml ./webapp/WEB-INF/web.xml

CMD ["java", "-jar", "/workspace/mango-companion.jar", "/workspace/library"]