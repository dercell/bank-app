FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY . .

WORKDIR /app/notification
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/notification/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]