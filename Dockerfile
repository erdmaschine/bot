FROM maven:latest as build

WORKDIR /app

COPY src src
COPY pom.xml pom.xml

RUN ["mvn", "-B", "package", "--file", "pom.xml"]

FROM openjdk:slim as final

WORKDIR /app

COPY --from=build /app/target .

ENV JAVA_OPTS=""

CMD java $JAVA_OPTS -cp classes:dependency/* erdmaschine.bot.MainKt
