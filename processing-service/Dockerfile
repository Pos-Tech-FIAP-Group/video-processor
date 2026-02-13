FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

COPY . .

RUN mvn package -B -pl processing-service -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache ffmpeg

WORKDIR /app

COPY --from=build /build/processing-service/target/processing-service-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]