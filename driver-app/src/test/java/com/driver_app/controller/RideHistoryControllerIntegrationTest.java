package com.driver_app.controller;

import com.driver_app.service.DatabaseContainer;
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
import java.time.format.DateTimeFormatter;
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

    private final String driverUuid = "456e7890-f12e-34d5-b678-537425184001"; // Stała wartość dla przewidywalności testów

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
        registry.add("server.service.rides.url", () -> wireMockServer.baseUrl() + "/driver-app/api/rides");

        // Wyłącz Kafka w testach
        registry.add("spring.kafka.consumer.auto-startup", () -> "false");
        registry.add("spring.kafka.producer.auto-startup", () -> "false");
    }

    @Test
    public void testGetRideHistory() throws Exception {
        // Przygotowanie testowych danych historii przejazdów
        String testRideUuid = "test-ride-driver-123";
        LocalDateTime testCreatedAt = LocalDateTime.now();

        RideHistoryDto historyEntry = createRideHistoryDto(
                testRideUuid,
                testCreatedAt,
                RideStatus.COMPLETED,
                new BigDecimal("35.50"),
                50.2241,
                18.9868,
                50.2588,
                19.0169);

        // Mockowanie odpowiedzi WireMock
        stubFor(WireMock.get(urlPathEqualTo("/driver-app/api/rides/driver/" + driverUuid + "/history"))
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
        mockMvc.perform(get("/driver-app/api/rides/driver/{driverUuid}/history", driverUuid)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].rideUuid").value(testRideUuid))
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.content[0].amount").value(35.50))
                .andExpect(jsonPath("$.content[0].pickupLocationLatitude").value(50.2241));
    }

    @Test
    public void testGetRideHistoryWithFilters() throws Exception {
        // Przygotowanie testowych danych
        String testRideUuid = "test-ride-filtered-driver-456";
        LocalDateTime testCreatedAt = LocalDateTime.now();

        RideHistoryDto historyEntry = createRideHistoryDto(
                testRideUuid,
                testCreatedAt,
                RideStatus.COMPLETED,
                new BigDecimal("42.75"),
                50.2241,
                18.9868,
                50.2588,
                19.0169);

        // Przygotowanie dat
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        // Mockowanie odpowiedzi WireMock z elastycznym dopasowaniem dat
        stubFor(WireMock.get(urlPathEqualTo("/driver-app/api/rides/driver/" + driverUuid + "/history"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("size", equalTo("10"))
                .withQueryParam("sort", equalTo("createdAt,desc"))
                .withQueryParam("status", equalTo("COMPLETED"))
                .withQueryParam("startDate", matching(".*"))
                .withQueryParam("endDate", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("X-Total-Count", "1")
                        .withHeader("X-Total-Pages", "1")
                        .withBody(createJsonResponse(historyEntry))));

        // Wykonanie żądania z filtrami
        mockMvc.perform(get("/driver-app/api/rides/driver/{driverUuid}/history", driverUuid)
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

    @Test
    public void testDownloadEarningsReport() throws Exception {
        // Przygotowanie testowych dat
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();

        // Przygotowanie przykładowej zawartości raportu CSV
        String csvContent = "date,ride_id,amount,status\n" +
                "2025-03-15T14:30:00,ride-123,35.50,COMPLETED\n" +
                "2025-03-20T08:45:00,ride-456,42.75,COMPLETED\n";

        // Mockowanie odpowiedzi serwisu
        stubFor(WireMock.get(urlPathEqualTo("/driver-app/api/rides/driver/" + driverUuid + "/earnings/report"))
                .withQueryParam("startDate", matching(".*"))
                .withQueryParam("endDate", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/csv")
                        .withHeader("Content-Disposition", "attachment; filename=earnings_report_" + driverUuid + ".csv")
                        .withBody(csvContent)));

        // Wykonanie żądania
        mockMvc.perform(get("/driver-app/api/rides/driver/{driverUuid}/earnings/report", driverUuid)
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_DATE_TIME)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"earnings_report_" + driverUuid + ".csv\""))
                .andExpect(content().bytes(csvContent.getBytes()));
    }

    @Test
    public void testDownloadEarningsReportWithDefaultDates() throws Exception {
        // Przygotowanie przykładowej zawartości raportu CSV
        String csvContent = "date,ride_id,amount,status\n" +
                "2025-03-01T12:30:00,ride-789,28.50,COMPLETED\n" +
                "2025-03-10T16:45:00,ride-012,37.25,COMPLETED\n";

        // Mockowanie odpowiedzi serwisu
        stubFor(WireMock.get(urlPathEqualTo("/driver-app/api/rides/driver/" + driverUuid + "/earnings/report"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/csv")
                        .withHeader("Content-Disposition", "attachment; filename=earnings_report_" + driverUuid + ".csv")
                        .withBody(csvContent)));

        // Wykonanie żądania bez parametrów dat
        mockMvc.perform(get("/driver-app/api/rides/driver/{driverUuid}/earnings/report", driverUuid))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"earnings_report_" + driverUuid + ".csv\""))
                .andExpect(content().bytes(csvContent.getBytes()));
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