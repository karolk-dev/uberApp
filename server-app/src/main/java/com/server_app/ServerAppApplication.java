package com.server_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
@EnableDiscoveryClient
@SpringBootApplication
@EnableKafka
public class ServerAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServerAppApplication.class, args);

	}
}

// TODO 20241022
// w kazdej aplikacji ustawic hibernate.ddl.auto=none
// kazda tabela ma byc tworzona za pomoca skryptu flyway ktore wrzucamy do resources/db.migration
//Przenieść Chat, Ride, Payment na serwer
//Przenieść DTO z ClientAPI na common-li

// ustanowic polaczenie kafka miedzy glownymi aplikacjami:
// tworzenie przejazdu przez żądanie klienta, szukanie kierowcy, akceptacja, powrot info do klienta

// TODO 20250325
// dockeryzacja: wszystkie aplikacje maja sie odpalac na dockerze. Ma byc mozliwosc odpalenia wiecej niz jednej takie zdockeryzowanej instancji kazdej aplikacji
// ogarnijcie zapisywanie logow do pliku (np za pomoca logbacka), Sprawdzcie, jak w dockerze dostac sie do folderu, w ktorym
// postawiona jest aplikacja i odnajdzcie ten plik z logami. Zrobcie to zarowno z poziomu aplikacji docker desktop,
// jak i za pomoca np powershella
// dodajac wasze aplikacje do docker compose zastosujcie tzw healthchecki (googlujcie co to), aby aplikacje wstawaly w odpowiedniej kolejnosci
// np eureka musi isc pierwsza, potem dopiero aplikacje ktore beda sie w niej rejestrowac, gdy docker sprawdzi, ze eureka jest 'zdrowa'
