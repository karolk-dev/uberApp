package com.server_app.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.server_app.dto.EarningsSummary;
import com.server_app.dto.RideHistorySearchCriteria;
import com.uber.common.dto.RideHistoryDto;
import com.uber.common.model.PaymentType;
import com.uber.common.model.RideStatus;
import com.uber.common.productSelector.Product;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
public class DriverEarningsServiceTest {

    @Mock
    private RideService rideService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DriverEarningsService driverEarningsService;


    @Captor
    private ArgumentCaptor<RideHistorySearchCriteria> searchCriteriaCaptor;
    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;
    @Captor
    private ArgumentCaptor<byte[]> reportBytesCaptor;
    @Captor
    private ArgumentCaptor<String> emailCaptor;

    private String testDriverUuid;
    private String testDriverEmail;
    private LocalDateTime testStartDate;
    private LocalDateTime testEndDate;
    private List<RideHistoryDto> rideList;
    private RideHistoryDto ride1;
    private RideHistoryDto ride2;
    private Product testProduct = Product.UberX;

    @BeforeEach
    void setUp() {
        testDriverUuid = "driver-uuid-123";
        testDriverEmail = "driver@example.com";
        testStartDate = LocalDateTime.of(2023, 10, 1, 0, 0);
        testEndDate = LocalDateTime.of(2023, 10, 31, 23, 59);

        ride1 = RideHistoryDto.builder()
                .rideUuid("ride-uuid-1")
                .createdAt(LocalDateTime.of(2023, 10, 15, 10, 0))
                .pickupLocationLatitude(50.0)
                .pickupLocationLongitude(19.0)
                .destinationLatitude(50.1)
                .destinationLongitude(19.1)
                .amount(new BigDecimal("25.50"))
                .currency("PLN")
                .isPaid(true)
                .paymentType(PaymentType.CARD)
                .status(RideStatus.COMPLETED)
                .clientName("Client One")
                .driverName("Driver One")
                .product(testProduct)
                .build();

        ride2 = RideHistoryDto.builder()
                .rideUuid("ride-uuid-2")
                .createdAt(LocalDateTime.of(2023, 10, 20, 15, 30))
                .pickupLocationLatitude(51.0)
                .pickupLocationLongitude(20.0)
                .destinationLatitude(51.1)
                .destinationLongitude(20.1)
                .amount(new BigDecimal("30.00"))
                .currency("PLN")
                .isPaid(true)
                .paymentType(PaymentType.CASH)
                .status(RideStatus.COMPLETED)
                .clientName("Client Two")
                .driverName("Driver Two")
                .product(testProduct)
                .build();

        rideList = Arrays.asList(ride1, ride2);
    }

    @Test
    @DisplayName("Generowanie raportu zarobków CSV - sukces")
    void generateEarningsReport_Success() throws IOException, CsvException {
        // Arrange
        Page<RideHistoryDto> ridePage = new PageImpl<>(rideList);
        PageRequest expectedPageRequest = PageRequest.of(0, Integer.MAX_VALUE);

        when(rideService.getDriverRideHistory(eq(testDriverUuid), any(RideHistorySearchCriteria.class), eq(expectedPageRequest)))
                .thenReturn(ridePage);


        byte[] csvBytes = driverEarningsService.generateEarningsReport(testDriverUuid, testStartDate, testEndDate);


        verify(rideService).getDriverRideHistory(eq(testDriverUuid), searchCriteriaCaptor.capture(), (PageRequest) pageableCaptor.capture());

        RideHistorySearchCriteria capturedCriteria = searchCriteriaCaptor.getValue();
        assertEquals(testStartDate, capturedCriteria.getStartDate());
        assertEquals(testEndDate, capturedCriteria.getEndDate());
        assertEquals(RideStatus.COMPLETED, capturedCriteria.getStatus());
        assertTrue(capturedCriteria.getIsPaid());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(Integer.MAX_VALUE, capturedPageable.getPageSize());

        assertNotNull(csvBytes);
        assertTrue(csvBytes.length > 0);

        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);
        try (CSVReader reader = new CSVReader(new StringReader(csvContent))) {
            List<String[]> lines = reader.readAll();
            assertEquals(3, lines.size());


            String[] expectedHeaders = {
                    "Date of ride", "Pickup location", "Destination location", "Fare amount", "Curreny",
                    "Payment status", "Payment type", "Ride ID"
            };

            assertArrayEquals(expectedHeaders, Arrays.copyOfRange(lines.get(0), 0, expectedHeaders.length));


            String[] dataRow1 = lines.get(1);
            assertEquals(10, dataRow1.length);
            assertEquals(ride1.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), dataRow1[0]);
            assertEquals(String.valueOf(ride1.getPickupLocationLatitude()), dataRow1[1]);
            assertEquals(String.valueOf(ride1.getPickupLocationLongitude()), dataRow1[2]);
            assertEquals(String.valueOf(ride1.getDestinationLatitude()), dataRow1[3]);
            assertEquals(String.valueOf(ride1.getDestinationLongitude()), dataRow1[4]);
            assertEquals(ride1.getAmount().toString(), dataRow1[5]);
            assertEquals(ride1.getCurrency(), dataRow1[6]);
            assertEquals("Opłacony", dataRow1[7]);
            assertEquals(ride1.getPaymentType().toString(), dataRow1[8]);
            assertEquals(ride1.getRideUuid(), dataRow1[9]);


            String[] dataRow2 = lines.get(2);
            assertEquals(10, dataRow2.length);
            assertEquals(ride2.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), dataRow2[0]);
            assertEquals(String.valueOf(ride2.getPickupLocationLatitude()), dataRow2[1]);
            assertEquals(String.valueOf(ride2.getPickupLocationLongitude()), dataRow2[2]);
            assertEquals(String.valueOf(ride2.getDestinationLatitude()), dataRow2[3]);
            assertEquals(String.valueOf(ride2.getDestinationLongitude()), dataRow2[4]);
            assertEquals(ride2.getAmount().toString(), dataRow2[5]);
            assertEquals(ride2.getCurrency(), dataRow2[6]);
            assertEquals("Opłacony", dataRow2[7]);
            assertEquals(ride2.getPaymentType().toString(), dataRow2[8]);
            assertEquals(ride2.getRideUuid(), dataRow2[9]);
        }
    }

    @Test
    @DisplayName("Generowanie raportu zarobków CSV - brak przejazdów")
    void generateEarningsReport_NoRides() throws IOException, CsvException {

        Page<RideHistoryDto> emptyPage = new PageImpl<>(Collections.emptyList());
        PageRequest expectedPageRequest = PageRequest.of(0, Integer.MAX_VALUE);
        when(rideService.getDriverRideHistory(eq(testDriverUuid), any(RideHistorySearchCriteria.class), eq(expectedPageRequest)))
                .thenReturn(emptyPage);


        byte[] csvBytes = driverEarningsService.generateEarningsReport(testDriverUuid, testStartDate, testEndDate);


        verify(rideService).getDriverRideHistory(eq(testDriverUuid), any(RideHistorySearchCriteria.class), eq(expectedPageRequest));

        assertNotNull(csvBytes);
        assertTrue(csvBytes.length > 0);

        String csvContent = new String(csvBytes, StandardCharsets.UTF_8);
        try (CSVReader reader = new CSVReader(new StringReader(csvContent))) {
            List<String[]> lines = reader.readAll();
            assertEquals(1, lines.size());

            String[] expectedHeaders = {
                    "Date of ride", "Pickup location", "Destination location", "Fare amount", "Curreny",
                    "Payment status", "Payment type", "Ride ID"
            };

            assertArrayEquals(expectedHeaders, Arrays.copyOfRange(lines.get(0), 0, expectedHeaders.length));
        }
    }

    @Test
    @DisplayName("Generowanie i wysyłanie raportu - sukces")
    void generateAndSendEarningsReport_Success() throws MessagingException, ExecutionException, InterruptedException {
        // Arrange
        Page<RideHistoryDto> ridePage = new PageImpl<>(rideList);
        PageRequest expectedPageRequest = PageRequest.of(0, Integer.MAX_VALUE);

        when(rideService.getDriverRideHistory(eq(testDriverUuid), any(RideHistorySearchCriteria.class), eq(expectedPageRequest)))
                .thenReturn(ridePage);
        doNothing().when(emailService).sendEarningsReport(anyString(), any(byte[].class));

        // Act
        CompletableFuture<Void> future = driverEarningsService.generateAndSendEarningsReport(
                testDriverUuid, testDriverEmail, testStartDate, testEndDate);
        future.get();

        // Assert
        verify(rideService).getDriverRideHistory(eq(testDriverUuid), any(RideHistorySearchCriteria.class), eq(expectedPageRequest));
        verify(emailService).sendEarningsReport(emailCaptor.capture(), reportBytesCaptor.capture());

        assertEquals(testDriverEmail, emailCaptor.getValue());
        byte[] capturedBytes = reportBytesCaptor.getValue();
        assertNotNull(capturedBytes);
        assertTrue(capturedBytes.length > 0);

        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Generowanie i wysyłanie raportu - błąd wysyłki email")
    void generateAndSendEarningsReport_EmailFailure() throws MessagingException {
        // Arrange
        Page<RideHistoryDto> ridePage = new PageImpl<>(rideList);
        PageRequest expectedPageRequest = PageRequest.of(0, Integer.MAX_VALUE);

        when(rideService.getDriverRideHistory(eq(testDriverUuid), any(RideHistorySearchCriteria.class), eq(expectedPageRequest)))
                .thenReturn(ridePage);

        MessagingException expectedException = new MessagingException("Błąd serwera SMTP");
        doThrow(expectedException).when(emailService).sendEarningsReport(anyString(), any(byte[].class));


        MessagingException thrown = assertThrows(MessagingException.class, () -> {
            driverEarningsService.generateAndSendEarningsReport(testDriverUuid, testDriverEmail, testStartDate, testEndDate);

        });

        assertEquals(expectedException.getMessage(), thrown.getMessage());
        verify(emailService).sendEarningsReport(eq(testDriverEmail), any(byte[].class));
    }

    @Test
    @DisplayName("Obliczanie podsumowania zarobków - pusta lista")
    void calculateEarningsSummary_EmptyList() {

        List<RideHistoryDto> emptyList = Collections.emptyList();


        BigDecimal expectedTotalEarnings = BigDecimal.ZERO;
        int expectedTotalRides = 0;
        BigDecimal expectedAverage = BigDecimal.ZERO;
        Map<String, BigDecimal> expectedEarningsByPaymentType = Collections.emptyMap();


        EarningsSummary summary = driverEarningsService.calculateEarningsSummary(emptyList);


        assertNotNull(summary);
        assertEquals(expectedTotalRides, summary.getTotalRides());
        assertEquals(0, expectedTotalEarnings.compareTo(summary.getTotalEarnings()));
        assertEquals(0, expectedAverage.compareTo(summary.getAveragePerRide()));
        assertEquals(expectedEarningsByPaymentType, summary.getEarningsByPaymentType());
    }

    @Test
    @DisplayName("Obliczanie podsumowania zarobków - jeden przejazd")
    void calculateEarningsSummary_SingleRide() {

        List<RideHistoryDto> singleRideList = Collections.singletonList(ride1);


        BigDecimal expectedTotalEarnings = new BigDecimal("25.50");
        int expectedTotalRides = 1;
        BigDecimal expectedAverage = new BigDecimal("25.50");
        Map<String, BigDecimal> expectedEarningsByPaymentType = Map.of(
                PaymentType.CARD.toString(), new BigDecimal("25.50")
        );


        EarningsSummary summary = driverEarningsService.calculateEarningsSummary(singleRideList);


        assertNotNull(summary);
        assertEquals(expectedTotalRides, summary.getTotalRides());
        assertEquals(0, expectedTotalEarnings.compareTo(summary.getTotalEarnings()));
        assertEquals(0, expectedAverage.compareTo(summary.getAveragePerRide()));
        assertEquals(expectedEarningsByPaymentType.size(), summary.getEarningsByPaymentType().size());
        assertEquals(0, expectedEarningsByPaymentType.get(PaymentType.CARD.toString()).compareTo(summary.getEarningsByPaymentType().get(PaymentType.CARD.toString())));
    }




}
