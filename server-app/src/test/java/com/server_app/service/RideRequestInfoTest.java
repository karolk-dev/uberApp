package com.server_app.service;

import com.server_app.routing.GoogleRoutesService;
import com.server_app.routing.RouteInfo;
import com.uber.common.Coordinates;
import com.uber.common.dto.DriverDto;
import com.uber.common.productSelector.RideDataInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideRequestInfoTest {

    @Mock
    private KafkaTemplate<String, RideDataInfoDto> kafkaTemplate;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GoogleRoutesService googleRoutesService;

    @InjectMocks
    private RideRequestInfo rideRequestInfo;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(rideRequestInfo, "driverServiceUrl", "http://test-driver-service");
    }

    @Test
    public void testProcessRideRequestSuccess() throws Exception {
        // given
        Coordinates pickupLocation = new Coordinates(40.0, -74.0);
        Coordinates destinationLocation = new Coordinates(41.0, -75.0);

        DriverDto driverDto = DriverDto.builder()
                .coordinates(new Coordinates(40.5, -74.5))
                .build();
        Set<DriverDto> driversInRange = new HashSet<>();
        driversInRange.add(driverDto);
        ResponseEntity<Set<DriverDto>> driversResponse = ResponseEntity.ok(driversInRange);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(driversResponse);

        RouteInfo mainRoute = new RouteInfo();
        mainRoute.setDistanceInMeters(10000); // 10 km
        mainRoute.setPolyline("test-polyline");
        mainRoute.setEtaInMinutes(5);
        when(googleRoutesService.getRouteInfo(eq(pickupLocation), eq(destinationLocation))).thenReturn(mainRoute);

        RouteInfo driverRoute = new RouteInfo();
        driverRoute.setDistanceInMeters(8000);
        driverRoute.setPolyline("driver-polyline");
        driverRoute.setEtaInMinutes(10);
        when(googleRoutesService.getRouteInfo(eq(driverDto.getCoordinates()), eq(destinationLocation)))
                .thenReturn(driverRoute);

        // when
        RideDataInfoDto result = rideRequestInfo.processRideRequest(pickupLocation, destinationLocation);

        // then
        assertNotNull(result);
        assertEquals("test-polyline", result.getPolyline());
        assertEquals(10, result.getDistance());
        assertEquals(10, result.getEta());
        assertNotNull(result.getUberComfortPrice());
        assertNotNull(result.getUberGreenPrice());
        assertNotNull(result.getUberXPrice());
        assertNotNull(result.getUberPetsPrice());
    }

    @Test
    public void testGetDriversInRange() {
        // given
        Coordinates coordinates = new Coordinates(40.0, -74.0);
        double radiusInKm = 10.0;
        Set<DriverDto> expectedDrivers = new HashSet<>();
        expectedDrivers.add(DriverDto.builder().build());
        ResponseEntity<Set<DriverDto>> responseEntity = ResponseEntity.ok(expectedDrivers);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // when
        Set<DriverDto> result = rideRequestInfo.getDriversInRange(coordinates, radiusInKm);

        // then
        assertNotNull(result);
        assertEquals(expectedDrivers, result);
    }
}
