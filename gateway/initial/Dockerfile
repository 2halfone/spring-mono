FROM openjdk:11-jre-slim
VOLUME /tmp
ARG JAR_FILE=target/gateway-service-*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]