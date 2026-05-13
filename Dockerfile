# Stage 1: Build
FROM maven:3.8-openjdk-17 AS build
WORKDIR /workspace

# Cache dependencies first
COPY pom.xml .
COPY campus-help-common/pom.xml campus-help-common/
COPY campus-help-user/pom.xml campus-help-user/
COPY campus-help-order/pom.xml campus-help-order/
COPY campus-help-product/pom.xml campus-help-product/
COPY campus-help-life/pom.xml campus-help-life/
COPY campus-help-server/pom.xml campus-help-server/
RUN mvn dependency:go-offline -B -pl campus-help-server -am || true

# Full source & build
COPY . .
RUN mvn clean package -DskipTests -B -pl campus-help-server -am

# Stage 2: Runtime
FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=build /workspace/campus-help-server/target/campus-help-server-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
