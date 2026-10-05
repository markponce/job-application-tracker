FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY . .
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar

ENV GG_JTE_DEVELOPMENT_MODE=false \
    GG_JTE_USE_PRECOMPILED_TEMPLATES=true

RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]