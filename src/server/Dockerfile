FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml ./
COPY dao ./dao
COPY model ./model
COPY service ./service
COPY utils ./utils

RUN mvn clean compile dependency:copy-dependencies -DincludeScope=runtime

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/target/classes /app/classes
COPY --from=builder /build/target/dependency /app/dependency

EXPOSE 9999

CMD ["java", "-cp", "/app/classes:/app/dependency/*", "service.facade.JavaFacadeWrapper"]