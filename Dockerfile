FROM maven:latest as build

WORKDIR /app

COPY ["src", "pom.xml", "/"]

RUN ["mvn", "-B", "package", "--file", "pom.xml"]

FROM openjdk:slim as final

WORKDIR /app

COPY --from=build /app/target .

CMD ["java", "-cp",  "classes:dependency/*", "erdmaschine.bot.MainKt"]
