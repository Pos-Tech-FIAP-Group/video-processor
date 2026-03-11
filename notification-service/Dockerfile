FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

COPY . .

RUN mvn clean package -B -pl notification-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /build/notification-service/target/notification-service-*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]