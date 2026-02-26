# Build with: mvn clean package
# Run with: java -jar target/claims-service-1.0.0.jar

FROM maven:3.8-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/claims-service-*.jar claims-service.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "claims-service.jar"]

