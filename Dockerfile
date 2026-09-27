FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y --no-install-recommends gradle && rm -rf /var/lib/apt/lists/*
RUN gradle clean bootJar --no-daemon
EXPOSE 8080
CMD ["java", "-jar", "build/libs/ToDoProject-0.0.1-SNAPSHOT.jar"]
