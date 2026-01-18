FROM openjdk:21-jdk

WORKDIR /app

# Kopiowanie pliku JAR aplikacji
COPY target/client-app-0.0.1-SNAPSHOT.jar /app/client-app.jar

# Port, na którym działa aplikacja
#EXPOSE 8082

# Uruchomienie aplikacji
ENTRYPOINT ["java", "-jar", "/app/client-app.jar"]