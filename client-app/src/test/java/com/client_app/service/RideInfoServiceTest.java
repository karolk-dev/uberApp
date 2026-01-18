package com.client_app.service;

import com.uber.common.Coordinates;
import com.uber.common.productSelector.RideDataInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideInfoServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RideInfoService rideInfoService;

//    @BeforeEach
//    void setUp() {
//        rideInfoService = new RideInfoService(restTemplate);
//        String serviceUrl = "http://example.com/api";
//        ReflectionTestUtils.setField(rideInfoService, "serverServiceUrl", serviceUrl);
//    }


    @Test
    void getRideInfo_returnsRideDataInfoDto() {
//        // Given - przykładowe współrzędne dla pickup i destination
//        Coordinates pickup = new Coordinates(40.7128, -74.0060);          // np. Nowy Jork
//        Coordinates destination = new Coordinates(34.0522, -118.2437);      // np. Los Angeles
//
//        RideDataInfoDto expectedDto = new RideDataInfoDto();
//        // Możesz ustawić pola expectedDto, jeśli klasa je posiada
//
//        ResponseEntity<RideDataInfoDto> responseEntity = ResponseEntity.ok(expectedDto);
//        // Stubujemy wywołanie exchange() tak, aby zwracało przygotowaną odpowiedź
//        when(restTemplate.exchange(
//                anyString(),
//                eq(HttpMethod.GET),
//                isNull(),
//                ArgumentMatchers.<ParameterizedTypeReference<RideDataInfoDto>>any()
//        )).thenReturn(responseEntity);
//
//
//        // When - wywołujemy metodę getRideInfo
//        RideDataInfoDto result = rideInfoService.getRideInfo(pickup, destination);
//
//        // Then - sprawdzamy, czy metoda zwróciła oczekiwany obiekt
//        assertEquals(expectedDto, result);
//
//        // Weryfikacja, czy RestTemplate.exchange() został wywołany z poprawnym URL-em
//        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
//        verify(restTemplate).exchange(
//                urlCaptor.capture(),
//                eq(HttpMethod.GET),
//                isNull(),
//                ArgumentMatchers.<ParameterizedTypeReference<RideDataInfoDto>>any()
//        );
//        String calledUrl = urlCaptor.getValue();
//
//        assertTrue(calledUrl.contains("pickupLatitude=" + pickup.getLatitude()));
//        assertTrue(calledUrl.contains("pickupLongitude=" + pickup.getLongitude()));
//        assertTrue(calledUrl.contains("destinationLatitude=" + destination.getLatitude()));
//        assertTrue(calledUrl.contains("destinationLongitude=" + destination.getLongitude()));
    }
}
