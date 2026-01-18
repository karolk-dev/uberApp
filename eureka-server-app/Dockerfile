FROM openjdk:21-jdk

WORKDIR /app

# Kopiowanie pliku JAR aplikacji
COPY target/eureka-server-app-1.0-SNAPSHOT.jar /app/eureka-server.jar

# Port, na którym działa Eureka Server (standardowo 8761)
EXPOSE 8761

# Uruchomienie aplikacji
ENTRYPOINT ["java", "-jar", "/app/eureka-server.jar"]