# Çok aşamalı derleme: docker compose build öncesi yerel mvn package zorunlu değil.
FROM maven:3.9-eclipse-temurin-17 AS builder

ARG MODULE
WORKDIR /workspace

COPY . .

RUN mvn -pl "${MODULE}" -am package -DskipTests -B \
    && JAR="$(find "${MODULE}/target" -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' | head -n1)" \
    && test -n "$JAR" \
    && cp "$JAR" /tmp/app.jar

FROM eclipse-temurin:17-jre-alpine

ARG MODULE
WORKDIR /app

COPY --from=builder /tmp/app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
