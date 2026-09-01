FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .

COPY ./transfer /app/transfer

WORKDIR /app/transfer
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/transfer/target/*.jar app.jar
EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]