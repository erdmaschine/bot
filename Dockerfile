FROM openjdk:17-slim as final

WORKDIR /app

COPY target/erdmaschine-bot-1.0.0-jar-with-dependencies.jar .

CMD ["java", "-jar", "erdmaschine-bot-1.0.0-jar-with-dependencies.jar"]
