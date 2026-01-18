package com.driver_app.service;

import com.uber.common.dto.RestResponsePage;
import com.uber.common.dto.RideHistoryDto;
import com.uber.common.dto.RideHistoryFilterDto;
import com.uber.common.model.PaymentType;
import com.uber.common.model.RideStatus;
import com.uber.common.productSelector.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideHistoryServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RideHistoryService rideHistoryService;

    private final String driverUuid = UUID.randomUUID().toString();
    private final String serverRidesUrl = "http://test-server/api/rides";
    private LocalDateTime testCreatedAt;

    @BeforeEach
    void setUp() {
        // Ustawienie wartości dla pola z adnotacją @Value
        ReflectionTestUtils.setField(rideHistoryService, "serverRidesUrl", serverRidesUrl);
        testCreatedAt = LocalDateTime.now();
    }

    @Test
    void testGetDriverRideHistory_WithoutFilters() {
        // Przygotowanie danych testowych
        List<RideHistoryDto> rideHistoryList = Arrays.asList(
                createRideHistoryDto("ride-1", RideStatus.COMPLETED, new BigDecimal("25.50"), Product.UberX),
                createRideHistoryDto("ride-2", RideStatus.COMPLETED, new BigDecimal("45.00"), Product.UberComfort)
        );

        // Tworzenie PageRequest dla paginacji
        Pageable pageable = PageRequest.of(0, 10);
        RestResponsePage<RideHistoryDto> page = new RestResponsePage<>(rideHistoryList, pageable, 2L);

        ResponseEntity<RestResponsePage<RideHistoryDto>> responseEntity = new ResponseEntity<>(page, HttpStatus.OK);

        // Mockowanie odpowiedzi z RestTemplate
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // Przygotowanie pustego obiektu filtrów
        RideHistoryFilterDto emptyFilterDto = new RideHistoryFilterDto();

        // Wykonanie metody testowanej
        Page<RideHistoryDto> result = rideHistoryService.getDriverRideHistory(driverUuid, emptyFilterDto, 0, 10, "createdAt,desc");

        // Weryfikacja
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2L);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);

        // Szczegółowa weryfikacja pierwszego elementu
        RideHistoryDto firstRide = result.getContent().get(0);
        assertThat(firstRide.getRideUuid()).isEqualTo("ride-1");
        assertThat(firstRide.getStatus()).isEqualTo(RideStatus.COMPLETED);
        assertThat(firstRide.getAmount()).isEqualByComparingTo(new BigDecimal("25.50"));
        assertThat(firstRide.getCurrency()).isEqualTo("PLN");
        assertThat(firstRide.getPaymentType()).isEqualTo(PaymentType.CARD);
        assertThat(firstRide.isPaid()).isTrue();
        assertThat(firstRide.getClientName()).isEqualTo("Jan");
        assertThat(firstRide.getDriverName()).isEqualTo("Piotr");
        assertThat(firstRide.getPickupLocationLatitude()).isEqualTo(50.2241);
        assertThat(firstRide.getPickupLocationLongitude()).isEqualTo(18.9868);
        assertThat(firstRide.getDestinationLatitude()).isEqualTo(50.2588);
        assertThat(firstRide.getDestinationLongitude()).isEqualTo(19.0169);
        assertThat(firstRide.getProduct()).isEqualTo(Product.UberX);
        assertThat(firstRide.getCreatedAt()).isEqualTo(testCreatedAt);

        // Szczegółowa weryfikacja drugiego elementu
        RideHistoryDto secondRide = result.getContent().get(1);
        assertThat(secondRide.getRideUuid()).isEqualTo("ride-2");
        assertThat(secondRide.getStatus()).isEqualTo(RideStatus.COMPLETED);
        assertThat(secondRide.getAmount()).isEqualByComparingTo(new BigDecimal("45.00"));
        assertThat(secondRide.getProduct()).isEqualTo(Product.UberComfort);

        // Weryfikacja, że odpowiednie URL zostało utworzone (bez parametrów filtrowania)
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class));

        String capturedUrl = urlCaptor.getValue();
        assertThat(capturedUrl).startsWith(serverRidesUrl + "/driver/" + driverUuid + "/history");
        assertThat(capturedUrl).contains("page=0");
        assertThat(capturedUrl).contains("size=10");
        assertThat(capturedUrl).contains("sort=createdAt,desc");
        assertThat(capturedUrl).doesNotContain("status=");
        assertThat(capturedUrl).doesNotContain("startDate=");
        assertThat(capturedUrl).doesNotContain("endDate=");
    }

    @Test
    void testGetDriverRideHistory_WithFilters() {
        // Przygotowanie danych testowych
        LocalDateTime rideCreatedAt = LocalDateTime.now().minusDays(3);
        List<RideHistoryDto> rideHistoryList = Arrays.asList(
                createRideHistoryDtoWithSpecificDate("ride-filtered", RideStatus.COMPLETED, new BigDecimal("30.00"), Product.UberComfort, rideCreatedAt)
        );

        // Tworzenie PageRequest dla paginacji
        Pageable pageable = PageRequest.of(0, 10);
        RestResponsePage<RideHistoryDto> page = new RestResponsePage<>(rideHistoryList, pageable, 1L);

        ResponseEntity<RestResponsePage<RideHistoryDto>> responseEntity = new ResponseEntity<>(page, HttpStatus.OK);

        // Mockowanie odpowiedzi z RestTemplate
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // Przygotowanie filtrów
        RideHistoryFilterDto filterDto = new RideHistoryFilterDto();
        filterDto.setStatus(RideStatus.COMPLETED);
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        filterDto.setStartDate(startDate);
        filterDto.setEndDate(endDate);

        // Wykonanie metody testowanej
        Page<RideHistoryDto> result = rideHistoryService.getDriverRideHistory(driverUuid, filterDto, 0, 10, "createdAt,desc");

        // Weryfikacja
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);

        // Szczegółowa weryfikacja elementu
        RideHistoryDto ride = result.getContent().get(0);
        assertThat(ride.getRideUuid()).isEqualTo("ride-filtered");
        assertThat(ride.getStatus()).isEqualTo(RideStatus.COMPLETED);
        assertThat(ride.getAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(ride.getCurrency()).isEqualTo("PLN");
        assertThat(ride.getPaymentType()).isEqualTo(PaymentType.CARD);
        assertThat(ride.isPaid()).isTrue();
        assertThat(ride.getClientName()).isEqualTo("Jan");
        assertThat(ride.getDriverName()).isEqualTo("Piotr");
        assertThat(ride.getPickupLocationLatitude()).isEqualTo(50.2241);
        assertThat(ride.getPickupLocationLongitude()).isEqualTo(18.9868);
        assertThat(ride.getDestinationLatitude()).isEqualTo(50.2588);
        assertThat(ride.getDestinationLongitude()).isEqualTo(19.0169);
        assertThat(ride.getProduct()).isEqualTo(Product.UberComfort);
        assertThat(ride.getCreatedAt()).isEqualTo(rideCreatedAt);

        // Weryfikacja, że odpowiednie URL zostało utworzone (z parametrami filtrowania)
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class));

        String capturedUrl = urlCaptor.getValue();
        assertThat(capturedUrl).startsWith(serverRidesUrl + "/driver/" + driverUuid + "/history");
        assertThat(capturedUrl).contains("page=0");
        assertThat(capturedUrl).contains("size=10");
        assertThat(capturedUrl).contains("sort=createdAt,desc");
        assertThat(capturedUrl).contains("status=COMPLETED");

        // Sprawdzenie formatu dat w URL
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        String expectedStartDate = URLEncoder.encode(startDate.format(formatter), StandardCharsets.UTF_8);
        String expectedEndDate = URLEncoder.encode(endDate.format(formatter), StandardCharsets.UTF_8);

        assertThat(capturedUrl).contains("startDate=");
        assertThat(capturedUrl).contains("endDate=");

        // Sprawdzenie czy URL zawiera ogólną część daty (rok-miesiąc-dzień)
        String startDateYearMonth = startDate.getYear() + "-" +
                String.format("%02d", startDate.getMonthValue()) + "-" +
                String.format("%02d", startDate.getDayOfMonth());
        String endDateYearMonth = endDate.getYear() + "-" +
                String.format("%02d", endDate.getMonthValue()) + "-" +
                String.format("%02d", endDate.getDayOfMonth());

        assertThat(capturedUrl).contains(startDateYearMonth);
        assertThat(capturedUrl).contains(endDateYearMonth);
    }

    @Test
    void testGetEarningsReport() {
        // Przygotowanie danych testowych
        byte[] reportContent = "Sample Report Content".getBytes();
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        // Mockowanie odpowiedzi z RestTemplate
        ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(reportContent, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class)))
                .thenReturn(responseEntity);

        // Wykonanie metody testowanej
        byte[] result = rideHistoryService.getEarningsReport(driverUuid, startDate, endDate);

        // Weryfikacja
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(reportContent);

        // Weryfikacja, że odpowiednie URL zostało utworzone
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class));

        String capturedUrl = urlCaptor.getValue();
        assertThat(capturedUrl).startsWith(serverRidesUrl + "/driver/" + driverUuid + "/earnings/report");
        assertThat(capturedUrl).contains("startDate=");
        assertThat(capturedUrl).contains("endDate=");

        // Sprawdzenie czy URL zawiera ogólną część daty (rok-miesiąc-dzień)
        String startDateYearMonth = startDate.getYear() + "-" +
                String.format("%02d", startDate.getMonthValue()) + "-" +
                String.format("%02d", startDate.getDayOfMonth());
        String endDateYearMonth = endDate.getYear() + "-" +
                String.format("%02d", endDate.getMonthValue()) + "-" +
                String.format("%02d", endDate.getDayOfMonth());

        assertThat(capturedUrl).contains(startDateYearMonth);
        assertThat(capturedUrl).contains(endDateYearMonth);
    }

    // Metoda pomocnicza do tworzenia obiektów RideHistoryDto
    private RideHistoryDto createRideHistoryDto(String rideUuid, RideStatus status, BigDecimal fareAmount, Product product) {
        return RideHistoryDto.builder()
                .rideUuid(rideUuid)
                .createdAt(testCreatedAt)
                .pickupLocationLatitude(50.2241)
                .pickupLocationLongitude(18.9868)
                .destinationLatitude(50.2588)
                .destinationLongitude(19.0169)
                .status(status)
                .amount(fareAmount)
                .currency("PLN")
                .paymentType(PaymentType.CARD)
                .clientName("Jan")
                .driverName("Piotr")
                .isPaid(true)
                .product(product)
                .build();
    }

    // Metoda pomocnicza umożliwiająca ustawienie konkretnej daty utworzenia
    private RideHistoryDto createRideHistoryDtoWithSpecificDate(String rideUuid, RideStatus status, BigDecimal fareAmount,
                                                                Product product, LocalDateTime createdAt) {
        return RideHistoryDto.builder()
                .rideUuid(rideUuid)
                .createdAt(createdAt)
                .pickupLocationLatitude(50.2241)
                .pickupLocationLongitude(18.9868)
                .destinationLatitude(50.2588)
                .destinationLongitude(19.0169)
                .status(status)
                .amount(fareAmount)
                .currency("PLN")
                .paymentType(PaymentType.CARD)
                .clientName("Jan")
                .driverName("Piotr")
                .isPaid(true)
                .product(product)
                .build();
    }
}