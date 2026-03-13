# Build (context = raiz do repositorio)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY notification-service/pom.xml notification-service/
COPY notification-service/src notification-service/src
# Build a partir do modulo para o spring-boot-maven-plugin repackage rodar corretamente
WORKDIR /app/notification-service
RUN mvn -B -DskipTests package spring-boot:repackage -q

# Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/notification-service/target/notification-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
