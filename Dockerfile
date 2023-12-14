FROM maven:latest as build

WORKDIR /app

COPY src src
COPY pom.xml pom.xml

RUN ["mvn", "-B", "package", "--file", "pom.xml"]

FROM openjdk:slim as final

WORKDIR /app

COPY --from=build /app/target .

RUN ["touch", "/var/erdmaschine.status"]

ENV JAVA_OPTS=""

CMD java $JAVA_OPTS -cp classes:dependency/* erdmaschine.bot.MainKt
