FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .

COPY ./accounts /app/accounts

WORKDIR /app/accounts
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/accounts/target/*.jar app.jar
EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]