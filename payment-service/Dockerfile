FROM maven:3.9.11-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY payment-service/pom.xml payment-service/
COPY payment-service/src payment-service/src

WORKDIR /app/payment-service

RUN mvn -B dependency:go-offline

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/payment-service/target/*.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]