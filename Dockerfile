FROM maven:latest as build

WORKDIR /app

COPY ["src/", "pom.xml", "./"]

RUN ["ls", "-la"]
RUN ["mvn", "-B", "package", "--file", "pom.xml"]

FROM openjdk:slim as final

WORKDIR /app

COPY --from=build /app/target .
RUN ["ls", "-la"]

CMD ["java", "-cp",  "classes:dependency/*", "erdmaschine.bot.MainKt"]
