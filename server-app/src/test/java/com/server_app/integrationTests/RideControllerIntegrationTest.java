package com.server_app.integrationTests;


import com.itextpdf.text.DocumentException;
import com.server_app.config.RideHistoryMapper;
import com.server_app.model.Ride;
import com.server_app.repository.RideRepository;
import com.server_app.service.DriverEarningsService;
import com.server_app.service.EmailService;
import com.server_app.service.PaymentService;
import com.server_app.service.RideRequestInfo;
import com.uber.common.dto.RideHistoryDto;
import com.uber.common.model.PaymentType;
import com.uber.common.model.RideStatus;
import com.uber.common.productSelector.Product;
import com.uber.common.productSelector.RideDataInfoDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"ride_requests", "ride_proposal"})
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RideControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;


    private final MockMvc postman;

    @Autowired
    private RideRepository rideRepository;


    @MockBean
    private PaymentService paymentService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private RideRequestInfo rideRequestInfo;

    @MockBean
    private RideHistoryMapper rideHistoryMapper;

    @MockBean
    private DriverEarningsService driverEarningsService;

//    @MockBean
//    private RideService rideService;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");
    @Autowired
    public RideControllerIntegrationTest(MockMvc postman) {
        this.postman = postman;
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.flyway.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgreSQLContainer::getUsername);
        registry.add("spring.flyway.password", postgreSQLContainer::getPassword);
    }


    @Test
    void testGetRideInfo() throws Exception {

        RideDataInfoDto infoDto = RideDataInfoDto.builder()
                .uberPetsPrice(200)
                .uberXPrice(100)
                .distance(3000)
                .uberComfortPrice(300)
                .eta(20)
                .build();
        when(rideRequestInfo.processRideRequest(any(), any())).thenReturn(infoDto);

        String url = "/api/rides/info?pickupLatitude=10.0&pickupLongitude=20.0&destinationLatitude=30.0&destinationLongitude=40.0";
        ResponseEntity<RideDataInfoDto> response = restTemplate.getForEntity(url, RideDataInfoDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void testPenaltyEndpoint() throws Exception {
        Ride ride = Ride.builder()
                .clientUuid("id")
                .uuid("id")
                .driverUuid(UUID.randomUUID().toString())
                .pickupLocationLongitude(50.50)
                .pickupLocationLatitude(50.50)
                .destinationLongitude(50.50)
                .destinationLatitude(50.50)
                .status(RideStatus.PENDING)
                .product(Product.UberX)
                .paymentIntendId("paymentIntentID")
                .customerId("id")
                .amount(1000)
                .polyline("polyline")
                .polylineToClient("polyline")
                .penaltyAmount(0)
                .build();
        rideRepository.save(ride);


        MvcResult result = postman.perform(post("/api/rides/penalty/id"))
                .andExpect(status().isOk())
                .andReturn();

        Ride updatedRideFromDB = rideRepository.findByUuid(ride.getUuid())
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));

        assertThat(updatedRideFromDB.getPenaltyAmount()).isEqualTo(30000);

    }

    @Test
    void testFinishRide() throws Exception {
        rideRepository.deleteAll();
        Ride ride = Ride.builder()
                .clientUuid("id")
                .uuid("id")
                .driverUuid(UUID.randomUUID().toString())
                .pickupLocationLongitude(50.50)
                .pickupLocationLatitude(50.50)
                .destinationLongitude(50.50)
                .destinationLatitude(50.50)
                .status(RideStatus.PENDING)
                .product(Product.UberX)
                .paymentIntendId("paymentIntentID")
                .customerId("id")
                .amount(1000)
                .polyline("polyline")
                .polylineToClient("polyline")
                .penaltyAmount(0)
                .build();
        rideRepository.save(ride);

        MvcResult result = postman.perform(post("/api/rides/finish/id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("COMPLETED"))
                .andReturn();

        Ride updatedRide = rideRepository.findById(ride.getId())
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));
        assertThat(updatedRide.getStatus()).isEqualTo(RideStatus.COMPLETED);

    }

    @Test
    public void sendInvoice_Success() throws Exception {
        // Given
        String testEmail = "test@example.com";
        doNothing().when(emailService).sendInvoiceEmail(eq(testEmail), any());

        // When & Then
        MvcResult result = postman.perform(
                        post("/api/rides/invoice")
                                .param("email", testEmail)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Faktura została wysłana na adres: " + testEmail))
                .andReturn();

        verify(emailService).sendInvoiceEmail(eq(testEmail), any());
    }

    @Test
    public void sendInvoice_DocumentException() throws Exception {
//        // Given
//        String testEmail = "test@example.com";
//        doThrow(new DocumentException("Test document exception"))
//                .when(emailService).sendInvoiceEmail(eq(testEmail), any());
//
//        // When & Then
//        postman.perform(
//                        post("/api/rides/invoice")
//                                .param("email", testEmail)
//                                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError())
//                .andExpect(content().string("blad"));
//
//        verify(emailService).sendInvoiceEmail(eq(testEmail), any());
    }

    @Test
    public void sendInvoice_IOException() throws Exception {
//        // Given
//        String testEmail = "test@example.com";
//        doThrow(new IOException("Test IO exception"))
//                .when(emailService).sendInvoiceEmail(eq(testEmail), any());
//
//        // When & Then
//        postman.perform(
//                        post("/api/rides/invoice")
//                                .param("email", testEmail)
//                                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isInternalServerError())
//                .andExpect(content().string("blad"));
//
//        verify(emailService).sendInvoiceEmail(eq(testEmail), any());
    }

    @Test
    public void sendInvoice_RuntimeException() throws Exception {
        // Given
        String testEmail = "test@example.com";
        doThrow(new RuntimeException("Test runtime exception"))
                .when(emailService).sendInvoiceEmail(eq(testEmail), any());

        // When & Then
        try {
            postman.perform(
                            post("/api/rides/invoice")
                                    .param("email", testEmail)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        } catch (Exception e) {
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
        }

        verify(emailService).sendInvoiceEmail(eq(testEmail), any());
    }


//    @Test
//    void testGetDriverRideHistory_DefaultParameters() {
//        // Given
//        int driverUuid = 123;
//        RideHistoryDto ride1 = createSampleRideHistoryDto("ride-1", LocalDateTime.now().minusDays(1));
//        RideHistoryDto ride2 = createSampleRideHistoryDto("ride-2", LocalDateTime.now().minusDays(2));
//
//        List<RideHistoryDto> rides = Arrays.asList(ride1, ride2);
//        Page<RideHistoryDto> ridePage = new PageImpl<>(rides, PageRequest.of(0, 10), rides.size());
//
//        when(rideHistoryMapper.toSearchCriteria(any(RideHistoryFilterDto.class)))
//                .thenReturn(new RideHistorySearchCriteria());
//
//        when(rideService.getDriverRideHistory(
//                eq(String.valueOf(driverUuid)),
//                any(RideHistorySearchCriteria.class),
//                any(PageRequest.class)))
//                .thenReturn(ridePage);
//
//        // When
//        String url = "/api/rides/driver/" + driverUuid + "/history";
//
//        // Alternatywnie, możemy użyć zwykłego obiektu zamiast Page
//        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
//
//        // Then
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).isNotNull();
//
//        // Sprawdzamy zawartość odpowiedzi jako Map
//        Map<String, Object> responseBody = response.getBody();
//        List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
//
//        assertThat(content).hasSize(2);
//        assertThat(content.get(0).get("rideUuid")).isEqualTo("ride-1");
//        assertThat(content.get(1).get("rideUuid")).isEqualTo("ride-2");
//
//        assertThat(response.getHeaders().get("X-Total-Count")).contains("2");
//        assertThat(response.getHeaders().get("X-Total-Pages")).contains("1");
//
//        // Verify proper PageRequest was created
//        verify(rideService).getDriverRideHistory(
//                eq(String.valueOf(driverUuid)),
//                any(RideHistorySearchCriteria.class),
//                any(PageRequest.class));
//    }

//    @Test
//    void testGetRideHistoryWithFilters() {
//        // Given
//        int driverUuid = 123;
//        RideHistoryDto ride = createSampleRideHistoryDto("ride-filtered", LocalDateTime.now().minusDays(1));
//        ride.setStatus(RideStatus.COMPLETED);
//        ride.setPaymentType(PaymentType.CARD);
//        ride.setFareAmount(BigDecimal.valueOf(30.00));
//
//        List<RideHistoryDto> rides = List.of(ride);
//        Page<RideHistoryDto> ridePage = new PageImpl<>(rides, PageRequest.of(0, 10), rides.size());
//
//        // Przygotowujemy kryteria wyszukiwania
//        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
//        LocalDateTime endDate = LocalDateTime.now();
//
//        RideHistorySearchCriteria expectedCriteria = RideHistorySearchCriteria.builder()
//                .startDate(startDate)
//                .endDate(endDate)
//                .status(RideStatus.COMPLETED)
//                .paymentType(PaymentType.CARD)
//                .isPaid(true)
//                .build();
//
//        when(rideHistoryMapper.toSearchCriteria(any(RideHistoryFilterDto.class)))
//                .thenReturn(expectedCriteria);
//
//        when(rideService.getDriverRideHistory(
//                eq(String.valueOf(driverUuid)),
//                eq(expectedCriteria),
//                any(PageRequest.class)))
//                .thenReturn(ridePage);
//
//        // When
//        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
//        String url = "/api/rides/driver/" + driverUuid + "/history" +
//                "?startDate=" + startDate.format(formatter) +
//                "&endDate=" + endDate.format(formatter) +
//                "&status=COMPLETED" +
//                "&isPaid=true" +
//                "&paymentType=CARD" +
//                "&minFare=20.00" +
//                "&maxFare=40.00";
//
//        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
//
//        // Then
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(response.getBody()).isNotNull();
//
//        // Sprawdzamy zawartość odpowiedzi
//        Map<String, Object> responseBody = response.getBody();
//        List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
//
//        assertThat(content).hasSize(1);
//        assertThat(content.get(0).get("rideUuid")).isEqualTo("ride-filtered");
//        assertThat(content.get(0).get("status")).isEqualTo("COMPLETED");
//        assertThat(content.get(0).get("paymentType")).isEqualTo("CARD");
//
//        // Weryfikujemy, że parametry filtrowania zostały przekazane do mappera
//        verify(rideHistoryMapper).toSearchCriteria(argThat(filter ->
//                filter.getStartDate() != null &&
//                        filter.getEndDate() != null &&
//                        filter.getStatus() == RideStatus.COMPLETED &&
//                        filter.getIsPaid() == Boolean.TRUE &&
//                        filter.getPaymentType() == PaymentType.CARD &&
//                        filter.getMinFare().compareTo(new BigDecimal("20.00")) == 0 &&
//                        filter.getMaxFare().compareTo(new BigDecimal("40.00")) == 0
//        ));
//
//        // Sprawdzamy wywołanie serwisu z kryteriami wyszukiwania
//        verify(rideService).getDriverRideHistory(
//                eq(String.valueOf(driverUuid)),
//                eq(expectedCriteria),
//                any(PageRequest.class));
//    }

    @Test
    void testDownloadEarningsReport() {
        // Given
        String driverUuid = "test-driver-uuid";
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        // Przygotowanie przykładowych danych CSV
        String csvContent = "Date of ride,Pickup location,Destination location,Fare amount,Curreny,Payment status,Payment type,Ride ID\n"
                + "2023-08-15 14:30,50.1,50.2,51.1,51.2,25.50,PLN,Opłacony,CARD,ride-123\n"
                + "2023-08-16 15:45,50.3,50.4,51.3,51.4,30.00,PLN,Opłacony,CASH,ride-456\n";
        byte[] mockedReportData = csvContent.getBytes(StandardCharsets.UTF_8);

        when(driverEarningsService.generateEarningsReport(
                eq(driverUuid),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(mockedReportData);

        // When
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        String url = "/api/rides/driver/" + driverUuid + "/earnings/report" +
                "?startDate=" + startDate.format(formatter) +
                "&endDate=" + endDate.format(formatter);

        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(mockedReportData);

        // Sprawdzamy nagłówki
        HttpHeaders headers = response.getHeaders();
        assertThat(headers.getContentType().toString()).isEqualTo("text/csv");
        assertThat(headers.getContentDisposition().getFilename())
                .isEqualTo("earnings_report_" + driverUuid + ".csv");

        // Weryfikujemy wywołanie serwisu
        verify(driverEarningsService).generateEarningsReport(
                eq(driverUuid),
                eq(startDate),
                eq(endDate)
        );
    }


    // Helper method to create sample RideHistoryDto objects
    private RideHistoryDto createSampleRideHistoryDto(String rideUuid, LocalDateTime createdAt) {
        return RideHistoryDto.builder()
                .rideUuid(rideUuid)
                .createdAt(createdAt)
                .pickupLocationLongitude(50.0)
                .pickupLocationLatitude(50.0)
                .destinationLongitude(51.0)
                .destinationLatitude(51.0)
                .status(RideStatus.COMPLETED)
                .amount(BigDecimal.valueOf(25.50))
                .currency("PLN")
                .isPaid(true)
                .paymentType(PaymentType.CASH)
                .clientName("Jan")
                .driverName("Marek")
                .build();
    }

}
