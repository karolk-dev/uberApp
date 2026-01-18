package com.client_app.service;

import com.uber.common.Coordinates;
import com.uber.common.productSelector.RideDataInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class RideInfoService {

    @Value("${server.service.ride-info.url}")
    private String rideInfoUrl;
    private final RestTemplate restTemplate;

    public RideDataInfoDto getRideInfo(Coordinates pickupLocation, Coordinates destinationLocation) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(rideInfoUrl)
                .queryParam("pickupLatitude", pickupLocation.getLatitude())
                .queryParam("pickupLongitude", pickupLocation.getLongitude())
                .queryParam("destinationLatitude", destinationLocation.getLatitude())
                .queryParam("destinationLongitude", destinationLocation.getLongitude());

        ParameterizedTypeReference<RideDataInfoDto> responseType =
                new ParameterizedTypeReference<>() {
                };

        ResponseEntity<RideDataInfoDto> response = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                null,
                responseType
        );
        return response.getBody();
    }
}