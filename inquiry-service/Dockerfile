FROM maven:3.9.11-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY inquiry-service/pom.xml inquiry-service/
COPY inquiry-service/src inquiry-service/src

WORKDIR /app/inquiry-service

RUN mvn -B dependency:go-offline

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/inquiry-service/target/*.jar app.jar

EXPOSE 8082
#TLS
EXPOSE 8443
#debug
EXPOSE 5005

ENTRYPOINT ["java", "-jar", "app.jar"]