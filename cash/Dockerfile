FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY . .

RUN ./mvnw -pl common -am clean install -DskipTests

WORKDIR /app/cash
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/cash/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]