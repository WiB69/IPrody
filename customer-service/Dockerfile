FROM maven:3.9.11-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY customer-service/pom.xml customer-service/
COPY customer-service/src customer-service/src

WORKDIR /app/customer-service

RUN mvn -B dependency:go-offline

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/customer-service/target/*.jar app.jar

EXPOSE 8081
#TLS
EXPOSE 8443
#debug
EXPOSE 5005

ENTRYPOINT ["java", "-jar", "app.jar"]