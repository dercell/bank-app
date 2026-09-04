FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY . .

WORKDIR /app/accounts
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/accounts/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]