FROM maven:latest as build

WORKDIR /app

COPY src src
COPY pom.xml pom.xml

RUN ["mvn", "-B", "compile", "--file", "pom.xml"]

FROM openjdk:slim as final

WORKDIR /app

COPY --from=build /app/target .

CMD ["java", "-cp",  "classes:dependency/*", "erdmaschine.bot.MainKt"]
