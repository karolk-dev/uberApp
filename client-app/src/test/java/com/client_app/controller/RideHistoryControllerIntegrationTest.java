package com.client_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.uber.common.dto.RideHistoryDto;
import com.uber.common.model.RideStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
public class RideHistoryControllerIntegrationTest extends DatabaseContainer {

    private final String clientUuid = "123e4567-e89b-12d3-a456-426614174000"; // Stała wartość dla przewidywalności testów

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeAll
    static void setup() {
        WireMock.configureFor(wireMockServer.getPort());
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("server.service.rides.url", () -> wireMockServer.baseUrl() + "/client-app/api/rides");

        // Wyłącz Kafka w testach - można to również przenieść do klasy bazowej
        registry.add("spring.kafka.consumer.auto-startup", () -> "false");
        registry.add("spring.kafka.producer.auto-startup", () -> "false");
    }

    @Test
    public void testGetRideHistory() throws Exception {
        // Przygotowanie testowych danych historii przejazdów
        String testRideUuid = "test-ride-123";
        LocalDateTime testCreatedAt = LocalDateTime.now();

        RideHistoryDto historyEntry = createRideHistoryDto(
                testRideUuid,
                testCreatedAt,
                RideStatus.COMPLETED,
                new BigDecimal("25.50"),
                50.2241,
                18.9868,
                50.2588,
                19.0169);

        // Mockowanie odpowiedzi WireMock - z użyciem bardziej bezpośredniego podejścia
        stubFor(WireMock.get(urlPathEqualTo("/client-app/api/rides/client/" + clientUuid + "/history"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("size", equalTo("10"))
                .withQueryParam("sort", equalTo("createdAt,desc"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("X-Total-Count", "1")
                        .withHeader("X-Total-Pages", "1")
                        .withBody(createJsonResponse(historyEntry))));

        // Wykonanie żądania i weryfikacja
        mockMvc.perform(get("/client-app/api/rides/client/{clientUuid}/history", clientUuid)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].rideUuid").value(testRideUuid))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.content[0].amount").value(25.50))
                .andExpect(jsonPath("$.content[0].pickupLocationLatitude").value(50.2241));
    }



    @Test
    public void testGetRideHistoryWithFilters() throws Exception {
        // Przygotowanie testowych danych
        String testRideUuid = "test-ride-filtered-123";
        LocalDateTime testCreatedAt = LocalDateTime.now();

        RideHistoryDto historyEntry = createRideHistoryDto(
                testRideUuid,
                testCreatedAt,
                RideStatus.COMPLETED,
                new BigDecimal("25.50"),
                50.2241,
                18.9868,
                50.2588,
                19.0169);

        // Przygotowanie dat - użyj matchingu wzorca zamiast dokładnego dopasowania
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        // Mockowanie odpowiedzi WireMock z elastycznym dopasowaniem dat
        stubFor(WireMock.get(urlPathEqualTo("/client-app/api/rides/client/" + clientUuid + "/history"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("size", equalTo("10"))
                .withQueryParam("sort", equalTo("createdAt,desc"))
                .withQueryParam("status", equalTo("COMPLETED"))
                .withQueryParam("startDate", matching(".*")) // Dopasuj dowolny format daty
                .withQueryParam("endDate", matching(".*"))   // Dopasuj dowolny format daty
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("X-Total-Count", "1")
                        .withHeader("X-Total-Pages", "1")
                        .withBody(createJsonResponse(historyEntry))));

        // Wykonanie żądania z filtrami
        mockMvc.perform(get("/client-app/api/rides/client/{clientUuid}/history", clientUuid)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc")
                        .param("status", "COMPLETED")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].rideUuid").value(testRideUuid))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));
    }

    private String createJsonResponse(RideHistoryDto historyEntry) throws Exception {
        List<RideHistoryDto> entries = List.of(historyEntry);
        PageImpl<RideHistoryDto> page = new PageImpl<>(entries, PageRequest.of(0, 10), entries.size());
        return objectMapper.writeValueAsString(page);
    }

    // Helper method to create RideHistoryDto objects for tests
    private RideHistoryDto createRideHistoryDto(
            String rideUuid,
            LocalDateTime createdAt,
            RideStatus status,
            BigDecimal amount,
            double pickupLat,
            double pickupLng,
            double destLat,
            double destLng) {

        return RideHistoryDto.builder()
                .rideUuid(rideUuid)
                .createdAt(createdAt)
                .status(status)
                .amount(amount)
                .isPaid(true)
                .pickupLocationLatitude(pickupLat)
                .pickupLocationLongitude(pickupLng)
                .destinationLatitude(destLat)
                .destinationLongitude(destLng)
                .build();
    }
}