# Build (context = raiz do repositório)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY auth-service/pom.xml auth-service/
COPY auth-service/src auth-service/src
# Build a partir do módulo para o spring-boot-maven-plugin repackage rodar corretamente
WORKDIR /app/auth-service
RUN mvn -B -DskipTests package spring-boot:repackage -q

# Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/auth-service/target/auth-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
