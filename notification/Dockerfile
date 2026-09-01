FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .

COPY ./notification /app/notification

WORKDIR /app/notification
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/notification/target/*.jar app.jar
EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]