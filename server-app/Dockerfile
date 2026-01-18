FROM openjdk:21-jdk

WORKDIR /app

# Kopiowanie pliku JAR aplikacji
COPY target/server-app-0.0.1-SNAPSHOT.jar /app/server-app.jar

## Port, na którym działa aplikacja
#EXPOSE 8081

# Uruchomienie aplikacji
ENTRYPOINT ["java", "-jar", "/app/server-app.jar"]