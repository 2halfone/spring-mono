FROM eclipse-temurin:17-jre
# Install wget for health checks
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY target/*.jar app.jar
COPY src/main/resources/application.yml application.yml
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
