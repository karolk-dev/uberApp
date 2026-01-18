package com.server_app.service;

import com.server_app.routing.GoogleRoutesService;
import com.server_app.routing.RouteInfo;
import com.uber.common.Coordinates;
import com.uber.common.dto.DriverDto;
import com.uber.common.model.DriverStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class DriverFinderTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DriverFinder driverFinder;

    @Mock
    private GoogleRoutesService googleRoutesService;


    @BeforeEach
    public void setUp() {

        ReflectionTestUtils.setField(driverFinder, "driverServiceUrl", "http://localhost:8080/drivers");
    }


    @Test
    public void testGetDriversInRange() {

        Coordinates centerPoint = new Coordinates(10.0, 20.0);
        DriverDto driverDto = DriverDto.builder()
                .uuid("uuid1")
                .name("jan")
                .nip("001")
                .companyName("asd")
                .coordinates(new Coordinates(10.1, 20.1))
                .companyStatus("status")
                .isAvailable(true)
                .status(DriverStatus.AVAILABLE)
                .build();
        List<DriverDto> driverList = Collections.singletonList(driverDto);
        ResponseEntity<List<DriverDto>> responseEntity = new ResponseEntity<>(driverList, HttpStatus.OK);


        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class))
        ).thenReturn(responseEntity);

        List<DriverDto> result = driverFinder.getDriversInRange(centerPoint, 10.0);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("uuid1", result.get(0).getUuid());


        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        );
        String url = urlCaptor.getValue();
        assertTrue(url.contains("latitude=10.0"));
        assertTrue(url.contains("longitude=20.0"));
        assertTrue(url.contains("radiusInKm=10.0"));
    }

    @Test
    public void testFindNearbyDriver_noDrivers() throws Exception {

        Coordinates centerPoint = new Coordinates(10.0, 20.0);
        ResponseEntity<List<DriverDto>> responseEntity = new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        DriverDto result = driverFinder.findNearbyDriver(centerPoint, 10, "someDriverUuid");
        assertNull(result);
    }

    @Test
    public void testFindNearbyDriver_filtersExcludedUuid() throws Exception {

        Coordinates centerPoint = new Coordinates(10.0, 20.0);
        DriverDto excludedDriver = DriverDto.builder()
                .uuid("driverUuid")
                .name("jan")
                .nip("001")
                .companyName("asd")
                .coordinates(new Coordinates(10.1, 20.1))
                .companyStatus("status")
                .isAvailable(true)
                .status(DriverStatus.AVAILABLE)
                .build();
        DriverDto validDriver = DriverDto.builder()
                .uuid("driver1")
                .name("jan")
                .nip("001")
                .companyName("asd")
                .coordinates(new Coordinates(10.2, 20.2))
                .companyStatus("status")
                .isAvailable(true)
                .status(DriverStatus.AVAILABLE)
                .build();

        List<DriverDto> drivers = Arrays.asList(excludedDriver, validDriver);
        ResponseEntity<List<DriverDto>> responseEntity = new ResponseEntity<>(drivers, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);


        RouteInfo routeInfoValid = RouteInfo.builder()
                .etaInMinutes(7)
                .build();

//        when(googleRoutesService.getRouteInfo(validDriver.getCoordinates(), centerPoint))
//                .thenReturn(routeInfoValid);


        DriverDto result = driverFinder.findNearbyDriver(centerPoint, 10, "driverUuid");
        assertNotNull(result);
        assertEquals("driver1", result.getUuid());
    }

    @Test
    public void testFindNearbyDriver_selectsDriverWithLowestEta() throws Exception {
        Coordinates centerPoint = new Coordinates(10.0, 20.0);
        DriverDto driver1 = DriverDto.builder()
                .uuid("driver1")
                .name("jan")
                .nip("001")
                .companyName("asd")
                .coordinates(new Coordinates(10.1, 20.1))
                .companyStatus("status")
                .isAvailable(true)
                .status(DriverStatus.AVAILABLE)
                .build();
        DriverDto driver2 = DriverDto.builder()
                .uuid("driver2")
                .name("jan")
                .nip("001")
                .companyName("asd")
                .coordinates(new Coordinates(10.2, 20.2))
                .companyStatus("status")
                .isAvailable(true)
                .status(DriverStatus.AVAILABLE)
                .build();
        List<DriverDto> drivers = Arrays.asList(driver1, driver2);
        ResponseEntity<List<DriverDto>> responseEntity = new ResponseEntity<>(drivers, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);


        RouteInfo routeInfo1 = RouteInfo.builder()
                .etaInMinutes(10)
                .build();
        RouteInfo routeInfo2 = RouteInfo.builder()
                .etaInMinutes(5)
                .build();
        when(googleRoutesService.getRouteInfo(driver1.getCoordinates(), centerPoint))
                .thenReturn(routeInfo1);
        when(googleRoutesService.getRouteInfo(driver2.getCoordinates(), centerPoint))
                .thenReturn(routeInfo2);

        DriverDto result = driverFinder.findNearbyDriver(centerPoint, 10, "anotherUuid");
        assertNotNull(result);

        assertEquals("driver2", result.getUuid());
    }

//    @Test
//    public void testFindNearbyDriver_whenGetRouteInfoThrowsException() throws Exception {
//        // Scenariusz: gdy metoda googleRoutesService.getRouteInfo rzuca wyjątek, metoda findNearbyDriver powinna również rzucić wyjątek.
//        Coordinates centerPoint = new Coordinates(10.0, 20.0);
//        DriverDto driver = DriverDto.builder()
//                .uuid("driver1")
//                .name("jan")
//                .nip("001")
//                .companyName("asd")
//                .coordinates(new Coordinates(10.1, 20.1))
//                .companyStatus("status")
//                .isAvailable(true)
//                .status(DriverStatus.AVAILABLE)
//                .build();
//        List<DriverDto> drivers = Collections.singletonList(driver);
//        ResponseEntity<List<DriverDto>> responseEntity = new ResponseEntity<>(drivers, HttpStatus.OK);
//        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
//                .thenReturn(responseEntity);
//
//        when(googleRoutesService.getRouteInfo(any(Coordinates.class), eq(centerPoint)))
//                .thenThrow(new RuntimeException("Route service error"));
//
//        assertThrows(RuntimeException.class, () -> {
//            driverFinder.findNearbyDriver(centerPoint, 10, "otherUuid");
//        });
//    }


}
