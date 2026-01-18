//package com.server_app.integrationTests;
//
//import com.server_app.config.RideHistoryMapper;
//import com.server_app.dto.RideHistorySearchCriteria;
//import com.server_app.service.RideService;
//import com.uber.common.dto.RideHistoryDto;
//import com.uber.common.dto.RideHistoryFilterDto;
//import com.uber.common.model.PaymentType;
//import com.uber.common.model.RideStatus;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.kafka.test.context.EmbeddedKafka;
//import org.springframework.test.annotation.DirtiesContext;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@EmbeddedKafka(partitions = 1, topics = {"ride_requests", "ride_proposal"})
//@AutoConfigureMockMvc
//@Testcontainers
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//public class DriverRideHistoryIntegrationTest {
//
//    @MockBean
//    private RideHistoryMapper rideHistoryMapper;
//
//    @MockBean
//    private RideService rideService;
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Test
//    void testGetDriverRideHistory_DefaultParameters() {
////        // Given
////        int driverUuid = 123;
////        RideHistoryDto ride1 = createSampleRideHistoryDto("ride-1", LocalDateTime.now().minusDays(1));
////        RideHistoryDto ride2 = createSampleRideHistoryDto("ride-2", LocalDateTime.now().minusDays(2));
////
////        List<RideHistoryDto> rides = Arrays.asList(ride1, ride2);
////        Page<RideHistoryDto> ridePage = new PageImpl<>(rides, PageRequest.of(0, 10), rides.size());
////
////        when(rideHistoryMapper.toSearchCriteria(any(RideHistoryFilterDto.class)))
////                .thenReturn(new RideHistorySearchCriteria());
////
////        when(rideService.getDriverRideHistory(
////                eq(String.valueOf(driverUuid)),
////                any(RideHistorySearchCriteria.class),
////                any(PageRequest.class)))
////                .thenReturn(ridePage);
////
////        // When
////        String url = "/api/rides/driver/" + driverUuid + "/history";
////
////        // Alternatywnie, możemy użyć zwykłego obiektu zamiast Page
////        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
////
////        // Then
////        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
////        assertThat(response.getBody()).isNotNull();
////
////        // Sprawdzamy zawartość odpowiedzi jako Map
////        Map<String, Object> responseBody = response.getBody();
////        List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
////
////        assertThat(content).hasSize(2);
////        assertThat(content.get(0).get("rideUuid")).isEqualTo("ride-1");
////        assertThat(content.get(1).get("rideUuid")).isEqualTo("ride-2");
////
////        assertThat(response.getHeaders().get("X-Total-Count")).contains("2");
////        assertThat(response.getHeaders().get("X-Total-Pages")).contains("1");
////
////        // Verify proper PageRequest was created
////        verify(rideService).getDriverRideHistory(
////                eq(String.valueOf(driverUuid)),
////                any(RideHistorySearchCriteria.class),
////                any(PageRequest.class));
//    }
//
//    @Test
//    void testGetRideHistoryWithFilters() {
////        // Given
////        int driverUuid = 123;
////        RideHistoryDto ride = createSampleRideHistoryDto("ride-filtered", LocalDateTime.now().minusDays(1));
////        ride.setStatus(RideStatus.COMPLETED);
////        ride.setPaymentType(PaymentType.CARD);
////        ride.setAmount(BigDecimal.valueOf(30.00));
////
////        List<RideHistoryDto> rides = List.of(ride);
////        Page<RideHistoryDto> ridePage = new PageImpl<>(rides, PageRequest.of(0, 10), rides.size());
////
////        // Przygotowujemy kryteria wyszukiwania
////        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
////        LocalDateTime endDate = LocalDateTime.now();
////
////        RideHistorySearchCriteria expectedCriteria = RideHistorySearchCriteria.builder()
////                .startDate(startDate)
////                .endDate(endDate)
////                .status(RideStatus.COMPLETED)
////                .paymentType(PaymentType.CARD)
////                .isPaid(true)
////                .build();
////
////        when(rideHistoryMapper.toSearchCriteria(any(RideHistoryFilterDto.class)))
////                .thenReturn(expectedCriteria);
////
////        when(rideService.getDriverRideHistory(
////                eq(String.valueOf(driverUuid)),
////                eq(expectedCriteria),
////                any(PageRequest.class)))
////                .thenReturn(ridePage);
////
////        // When
////        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
////        String url = "/api/rides/driver/" + driverUuid + "/history" +
////                "?startDate=" + startDate.format(formatter) +
////                "&endDate=" + endDate.format(formatter) +
////                "&status=COMPLETED" +
////                "&isPaid=true" +
////                "&paymentType=CARD" +
////                "&minFare=20.00" +
////                "&maxFare=40.00";
////
////        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
////
////        // Then
////        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
////        assertThat(response.getBody()).isNotNull();
////
////        // Sprawdzamy zawartość odpowiedzi
////        Map<String, Object> responseBody = response.getBody();
////        List<Map<String, Object>> content = (List<Map<String, Object>>) responseBody.get("content");
////
////        assertThat(content).hasSize(1);
////        assertThat(content.get(0).get("rideUuid")).isEqualTo("ride-filtered");
////        assertThat(content.get(0).get("status")).isEqualTo("COMPLETED");
////        assertThat(content.get(0).get("paymentType")).isEqualTo("CARD");
////
////        // Weryfikujemy, że parametry filtrowania zostały przekazane do mappera
////        verify(rideHistoryMapper).toSearchCriteria(argThat(filter ->
////                filter.getStartDate() != null &&
////                        filter.getEndDate() != null &&
////                        filter.getStatus() == RideStatus.COMPLETED &&
////                        filter.getIsPaid() == Boolean.TRUE &&
////                        filter.getPaymentType() == PaymentType.CARD &&
////                        filter.getMinFare().compareTo(new BigDecimal("20.00")) == 0 &&
////                        filter.getMaxFare().compareTo(new BigDecimal("40.00")) == 0
////        ));
////
////        // Sprawdzamy wywołanie serwisu z kryteriami wyszukiwania
////        verify(rideService).getDriverRideHistory(
////                eq(String.valueOf(driverUuid)),
////                eq(expectedCriteria),
////                any(PageRequest.class));
//    }
//
//    // Helper method to create sample RideHistoryDto objects
//    private RideHistoryDto createSampleRideHistoryDto(String rideUuid, LocalDateTime createdAt) {
//        return RideHistoryDto.builder()
//                .rideUuid(rideUuid)
//                .createdAt(createdAt)
//                .pickupLocationLongitude(50.0)
//                .pickupLocationLatitude(50.0)
//                .destinationLongitude(51.0)
//                .destinationLatitude(51.0)
//                .status(RideStatus.COMPLETED)
//                .amount(BigDecimal.valueOf(25.50))
//                .currency("PLN")
//                .isPaid(true)
//                .paymentType(PaymentType.CASH)
//                .clientName("Jan")
//                .driverName("Marek")
//                .build();
//    }
//
//}
