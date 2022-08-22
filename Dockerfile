FROM openjdk:17-slim as final

WORKDIR /app

COPY target .

CMD ["java", "-Xmx128m", "-cp",  "classes:dependency/*", "erdmaschine.bot.MainKt"]
