//package com.server_app.integrationTests;
//
//import com.server_app.config.RideHistoryMapper;
//import com.server_app.controller.RideController;
//import com.server_app.dto.RideHistorySearchCriteria;
//import com.server_app.service.RideService;
//import com.uber.common.dto.RideHistoryDto;
//import com.uber.common.dto.RideHistoryFilterDto;
//import com.uber.common.model.PaymentType;
//import com.uber.common.model.RideStatus;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
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
////@WebMvcTest(RideController.class)
////@AutoConfigureMockMvc
//@SpringBootTest
//@Testcontainers
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//public class ClientRideHistoryIntegrationTest {
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
//    void testGetClientRideHistory_DefaultParameters() {
////        // Given
////        String clientUuid = "client-123";
////        RideHistoryDto ride1 = createSampleRideHistoryDto("ride-1", LocalDateTime.now().minusDays(1));
////        RideHistoryDto ride2 = createSampleRideHistoryDto("ride-2", LocalDateTime.now().minusDays(2));
////
////        List<RideHistoryDto> rides = Arrays.asList(ride1, ride2);
////        Page<RideHistoryDto> ridePage = new PageImpl<>(rides, PageRequest.of(0, 10), rides.size());
////
////        when(rideHistoryMapper.toSearchCriteria(any(RideHistoryFilterDto.class)))
////                .thenReturn(new RideHistorySearchCriteria());
////
////        when(rideService.getClientRideHistory(
////                eq(clientUuid),
////                any(RideHistorySearchCriteria.class),
////                any(PageRequest.class)))
////                .thenReturn(ridePage);
////
////        // When
////        String url = "/api/rides/client/" + clientUuid + "/history";
////
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
////        verify(rideService).getClientRideHistory(
////                eq(clientUuid),
////                any(RideHistorySearchCriteria.class),
////                any(PageRequest.class));
//    }
//
//    @Test
//    void testGetClientRideHistoryWithFilters() {
////        // Given
////        String clientUuid = "client-123";
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
////        when(rideService.getClientRideHistory(
////                eq(clientUuid),
////                eq(expectedCriteria),
////                any(PageRequest.class)))
////                .thenReturn(ridePage);
////
////        // When
////        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
////        String url = "/api/rides/client/" + clientUuid + "/history" +
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
////        verify(rideService).getClientRideHistory(
////                eq(clientUuid),
////                eq(expectedCriteria),
////                any(PageRequest.class));
//    }
//
//    @Test
//    void testGetClientRideHistoryWithSorting() {
////        // Given
////        String clientUuid = "client-123";
////        RideHistoryDto ride1 = createSampleRideHistoryDto("ride-1", LocalDateTime.now().minusDays(1));
////        ride1.setAmount(BigDecimal.valueOf(30.00));
////        RideHistoryDto ride2 = createSampleRideHistoryDto("ride-2", LocalDateTime.now().minusDays(2));
////        ride2.setAmount(BigDecimal.valueOf(15.00));
////
////        List<RideHistoryDto> rides = Arrays.asList(ride1, ride2);
////        Page<RideHistoryDto> ridePage = new PageImpl<>(rides, PageRequest.of(0, 10), rides.size());
////
////        when(rideHistoryMapper.toSearchCriteria(any(RideHistoryFilterDto.class)))
////                .thenReturn(new RideHistorySearchCriteria());
////
////        when(rideService.getClientRideHistory(
////                eq(clientUuid),
////                any(RideHistorySearchCriteria.class),
////                any(PageRequest.class)))
////                .thenReturn(ridePage);
////
////        // When
////        String url = "/api/rides/client/" + clientUuid + "/history?sort=fareAmount,desc";
////
////        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
////
////        // Then
////        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
////        assertThat(response.getBody()).isNotNull();
////
////        // Verify proper PageRequest was created with sorting
////        verify(rideService).getClientRideHistory(
////                eq(clientUuid),
////                any(RideHistorySearchCriteria.class),
////                argThat(pageRequest ->
////                        pageRequest.getSort().getOrderFor("fareAmount") != null &&
////                                pageRequest.getSort().getOrderFor("fareAmount").isDescending()
////                ));
//    }
//
//    @Test
//    void testGetClientRideHistoryWithPagination() {
////        // Given
////        String clientUuid = "client-123";
////        List<RideHistoryDto> rides = Arrays.asList(
////                createSampleRideHistoryDto("ride-1", LocalDateTime.now().minusDays(1)),
////                createSampleRideHistoryDto("ride-2", LocalDateTime.now().minusDays(2))
////        );
////
////        // Page 2 z rozmiarem 2 (całkowita liczba elementów: 5)
////        Page<RideHistoryDto> ridePage = new PageImpl<>(rides, PageRequest.of(1, 2), 5);
////
////        when(rideHistoryMapper.toSearchCriteria(any(RideHistoryFilterDto.class)))
////                .thenReturn(new RideHistorySearchCriteria());
////
////        when(rideService.getClientRideHistory(
////                eq(clientUuid),
////                any(RideHistorySearchCriteria.class),
////                any(PageRequest.class)))
////                .thenReturn(ridePage);
////
////        // When
////        String url = "/api/rides/client/" + clientUuid + "/history?page=1&size=2";
////
////        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
////
////        // Then
////        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
////        assertThat(response.getBody()).isNotNull();
////
////        Map<String, Object> responseBody = response.getBody();
////
////        // Sprawdzamy informacje o paginacji
////        assertThat(responseBody.get("number")).isEqualTo(1);
////        assertThat(responseBody.get("size")).isEqualTo(2);
////        assertThat(responseBody.get("totalElements")).isEqualTo(5);
////        assertThat(responseBody.get("totalPages")).isEqualTo(3);
////
////        // Verify proper PageRequest was created with pagination parameters
////        verify(rideService).getClientRideHistory(
////                eq(clientUuid),
////                any(RideHistorySearchCriteria.class),
////                argThat(pageRequest ->
////                        pageRequest.getPageNumber() == 1 &&
////                                pageRequest.getPageSize() == 2
////                ));
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
//}