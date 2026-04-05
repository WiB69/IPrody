FROM maven:3.9.11-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY inventory-service/pom.xml inventory-service/
COPY inventory-service/src inventory-service/src

WORKDIR /app/inventory-service

RUN mvn -B dependency:go-offline

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/inventory-service/target/*.jar app.jar

EXPOSE 8083
#TLS
EXPOSE 8443
#debug
EXPOSE 5005

ENTRYPOINT ["java", "-jar", "app.jar"]