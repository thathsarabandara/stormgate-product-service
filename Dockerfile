FROM openjdk:21-jdk-slim as builder

WORKDIR /workspace

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline -q

COPY src src

RUN ./mvnw clean package -q -DskipTests

FROM openjdk:21-jdk-slim

RUN useradd -m spring
USER spring

WORKDIR /app

COPY --from=builder /workspace/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","app.jar"]
