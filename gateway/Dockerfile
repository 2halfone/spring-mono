FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=initial/target/gateway-service-*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
