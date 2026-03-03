# Build (context = raiz do repositório)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY video-service/pom.xml video-service/
COPY video-service/src video-service/src
WORKDIR /app/video-service
RUN mvn -B -DskipTests package spring-boot:repackage -q

# Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/video-service/target/video-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
