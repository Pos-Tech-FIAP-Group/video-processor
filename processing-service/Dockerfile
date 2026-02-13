# Build stage – Maven + Java 21 (matches pom.xml java.version)
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

# Copy full project so parent POM's modules (api-gateway, auth-service, etc.) all exist
COPY . .

# Build only processing-service (Java 21 per root pom.xml)
RUN mvn package -B -pl processing-service -DskipTests

# Run stage – Java 21 JRE
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /build/processing-service/target/processing-service-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
