FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/discovery-service-1.0.0.jar app.jar
# RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
ENTRYPOINT ["java","-jar","app.jar"]