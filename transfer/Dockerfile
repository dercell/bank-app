FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY . .

RUN ./mvnw -pl common -am clean install -DskipTests

WORKDIR /app/transfer
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/transfer/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]