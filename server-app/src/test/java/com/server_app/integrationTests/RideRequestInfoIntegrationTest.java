package com.server_app.integrationTests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.server_app.routing.GoogleRoutesService;
import com.server_app.service.RideRequestInfo;
import com.uber.common.Coordinates;
import com.uber.common.dto.DriverDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WireMockTest
public class RideRequestInfoIntegrationTest {

    private WireMockServer wireMockServer;
    private RideRequestInfo rideRequestInfo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        GoogleRoutesService googleRoutesService = mock(GoogleRoutesService.class);

        rideRequestInfo = new RideRequestInfo(
                mock(org.springframework.kafka.core.KafkaTemplate.class),
                restTemplate,
                googleRoutesService
        );


        ReflectionTestUtils.setField(
                rideRequestInfo,
                "driverServiceUrl",
                wireMock.baseUrl() + "/api/drivers"
        );
    }

    @Test
    void getDriversInRange_ReturnsDrivers() throws JsonProcessingException {
        // Given
        Coordinates coordinates = new Coordinates(52.2297, 21.0122);
        double radius = 10.0;
        DriverDto driver1 = DriverDto.builder()
                .name("driver-1")
                .uuid("uuid")
                .coordinates(coordinates)
                .build();
        DriverDto driver2 = DriverDto.builder()
                .name("driver-2")
                .uuid("uuid2")
                .coordinates(coordinates)
                .build();

        Set<DriverDto> expectedDrivers = Set.of(driver1, driver2);

        wireMock.stubFor(get(urlPathEqualTo("/api/drivers"))
                .withQueryParam("latitude", equalTo(String.valueOf(coordinates.getLatitude())))
                .withQueryParam("longitude", equalTo(String.valueOf(coordinates.getLongitude())))
                .withQueryParam("radiusInKm", equalTo(String.valueOf(radius)))
                .willReturn(okJson(objectMapper.writeValueAsString(expectedDrivers))));

        // When
        Set<DriverDto> result = rideRequestInfo.getDriversInRange(coordinates, radius);

        // Then
        assertThat(result)
                .extracting(DriverDto::getUuid)
                .containsExactlyInAnyOrder("uuid", "uuid2");
    }

    @Test
    void getDriversInRange_HandlesServerError() {
        // Given
        Coordinates coordinates = new Coordinates(40.7128, -74.0060);
        double radius = 5.0;

        wireMock.stubFor(get(urlPathEqualTo("/api/drivers"))
                .willReturn(serverError()));

        // When & Then
        assertThrows(HttpServerErrorException.class, () -> {
            rideRequestInfo.getDriversInRange(coordinates, radius);
        });
    }

    @Test
    void getDriversInRange_HandlesEmptyResponse() {
        // Given
        Coordinates coordinates = new Coordinates(0.0, 0.0);
        double radius = 1.0;

        wireMock.stubFor(get(urlPathEqualTo("/api/drivers"))
                .willReturn(okJson("[]")));

        // When
        Set<DriverDto> result = rideRequestInfo.getDriversInRange(coordinates, radius);

        // Then
        assertThat(result).isEmpty();
    }
}
