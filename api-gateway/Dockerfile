# Build (context = raiz do repositório)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY api-gateway/src api-gateway/src
# Build a partir do módulo para o spring-boot-maven-plugin repackage rodar corretamente
WORKDIR /app/api-gateway
RUN mvn -B -DskipTests package spring-boot:repackage -q

# Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar app.jar
COPY api-gateway/entrypoint.sh /app/entrypoint.sh
RUN sed -i 's/\r//g' /app/entrypoint.sh && chmod +x /app/entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["sh", "/app/entrypoint.sh"]
