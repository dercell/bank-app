FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY common/pom.xml common/
COPY accounts/pom.xml accounts/
COPY cash/pom.xml cash/
COPY transfer/pom.xml transfer/
COPY notification/pom.xml notification/
COPY front/pom.xml front/
RUN ./mvnw -pl accounts -am dependency:go-offline -B


COPY common/src common/src
COPY accounts/src accounts/src
RUN ./mvnw -pl accounts -am clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/accounts/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

