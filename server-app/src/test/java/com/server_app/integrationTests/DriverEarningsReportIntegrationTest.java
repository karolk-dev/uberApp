//package com.server_app.integrationTests;
//
//import com.server_app.service.DriverEarningsService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//public class DriverEarningsReportIntegrationTest {
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @MockBean
//    private DriverEarningsService driverEarningsService;
//
//    @Test
//    void testDownloadEarningsReport() {
////        // Given
////        String driverUuid = "test-driver-uuid";
////        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
////        LocalDateTime endDate = LocalDateTime.now();
////
////        // Przygotowanie przykładowych danych CSV
////        String csvContent = "Date of ride,Pickup location,Destination location,Fare amount,Curreny,Payment status,Payment type,Ride ID\n"
////                + "2023-08-15 14:30,50.1,50.2,51.1,51.2,25.50,PLN,Opłacony,CARD,ride-123\n"
////                + "2023-08-16 15:45,50.3,50.4,51.3,51.4,30.00,PLN,Opłacony,CASH,ride-456\n";
////        byte[] mockedReportData = csvContent.getBytes(StandardCharsets.UTF_8);
////
////        when(driverEarningsService.generateEarningsReport(
////                eq(driverUuid),
////                any(LocalDateTime.class),
////                any(LocalDateTime.class)
////        )).thenReturn(mockedReportData);
////
////        // When
////        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
////        String url = "/api/rides/driver/" + driverUuid + "/earnings/report" +
////                "?startDate=" + startDate.format(formatter) +
////                "&endDate=" + endDate.format(formatter);
////
////        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
////
////        // Then
////        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
////        assertThat(response.getBody()).isNotNull();
////        assertThat(response.getBody()).isEqualTo(mockedReportData);
////
////        // Sprawdzamy nagłówki
////        HttpHeaders headers = response.getHeaders();
////        assertThat(headers.getContentType().toString()).isEqualTo("text/csv");
////        assertThat(headers.getContentDisposition().getFilename())
////                .isEqualTo("earnings_report_" + driverUuid + ".csv");
////
////        // Weryfikujemy wywołanie serwisu
////        verify(driverEarningsService).generateEarningsReport(
////                eq(driverUuid),
////                eq(startDate),
////                eq(endDate)
////        );
//    }
//
//
//
//}
