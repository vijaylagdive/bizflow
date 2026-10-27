FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

COPY src src
RUN ./mvnw -q -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S bizflow && adduser -S bizflow -G bizflow
COPY --from=build /app/target/*.jar app.jar
RUN chown bizflow:bizflow app.jar
USER bizflow

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
