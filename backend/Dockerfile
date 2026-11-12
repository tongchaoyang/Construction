FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd -r app && useradd -r -g app app
COPY --from=build /build/target/backend.jar app.jar
USER app
EXPOSE 8080
USER root
#RUN addgroup -S spring && adduser -S spring -G spring 
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring
ENTRYPOINT ["java", "-jar", "app.jar"]
