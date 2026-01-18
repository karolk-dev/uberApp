package com.server_app.service;

import com.server_app.routing.GoogleRoutesService;
import com.server_app.routing.RouteInfo;
import com.uber.common.Coordinates;
import com.uber.common.dto.DriverDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverFinder {

    private final GoogleRoutesService googleRoutesService;
    private final RestTemplate restTemplate;
    @Value("${driver.service.url}")
    private String driverServiceUrl;
    private final int radius = 10;

    public DriverDto findNearbyDriver(Coordinates centerPoint, int radius, String driverUuid) throws Exception {
        return getDriversInRange(centerPoint, radius).stream()
                .filter(driverDto -> !driverDto.getUuid().equals(driverUuid))
                .min(Comparator.comparingInt(d -> {
                    RouteInfo routeInfo;
                    try {
                        routeInfo = googleRoutesService.getRouteInfo(d.getCoordinates(), centerPoint);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return routeInfo.getEtaInMinutes();
                }))
                .orElse(null);
    }

    public List<DriverDto> getDriversInRange(Coordinates centerPoint, double radiusInKm) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(driverServiceUrl)
                .queryParam("latitude", centerPoint.getLatitude())
                .queryParam("longitude", centerPoint.getLongitude())
                .queryParam("radiusInKm", radiusInKm);

        ParameterizedTypeReference<List<DriverDto>> responseType =
                new ParameterizedTypeReference<>() {
                };

        ResponseEntity<List<DriverDto>> response = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                null,
                responseType
        );

        return response.getBody();
    }
}
