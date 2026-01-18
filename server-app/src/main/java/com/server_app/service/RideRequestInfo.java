package com.server_app.service;

import com.server_app.routing.GoogleRoutesService;
import com.server_app.routing.RouteInfo;
import com.uber.common.Coordinates;
import com.uber.common.dto.DriverDto;
import com.uber.common.productSelector.Product;
import com.uber.common.productSelector.RideDataInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RideRequestInfo {

    private final KafkaTemplate<String, RideDataInfoDto> kafkaTemplate;
    private final RestTemplate restTemplate;
    private final GoogleRoutesService googleRoutesService;

    @Value("${driver.service.url}")
    private String driverServiceUrl;

    public RideDataInfoDto processRideRequest(Coordinates pickupLocation, Coordinates destinationLocation) throws Exception {
        RideDataInfoDto rideDataInfoDto = new RideDataInfoDto();
        Set<DriverDto> driversInRange = getDriversInRange(pickupLocation, 10);
        RouteInfo routeInfo;
        RouteInfo calculateRoad = googleRoutesService.getRouteInfo(pickupLocation, destinationLocation);
        System.out.println(pickupLocation + " " + destinationLocation);


        int minEta = Integer.MIN_VALUE;
        long distance = calculateRoad.getDistanceInMeters();


        for (DriverDto driverDto : driversInRange) {
            routeInfo = googleRoutesService.getRouteInfo(driverDto.getCoordinates(), destinationLocation);

            if (routeInfo.getEtaInMinutes() > minEta) {
                minEta = routeInfo.getEtaInMinutes();

            }
        }

        for (Product product : Product.values()) {
            switch (product) {
                case UberComfort:
                    rideDataInfoDto.setUberComfortPrice(RideCalculator.calculateRidePrice(Product.UberComfort, distance));
                    break;
                case UberGreen:
                    rideDataInfoDto.setUberGreenPrice(RideCalculator.calculateRidePrice(Product.UberGreen, distance));
                    break;
                case UberX:
                    rideDataInfoDto.setUberXPrice(RideCalculator.calculateRidePrice(Product.UberX, distance));
                    break;
                case UberPets:
                    rideDataInfoDto.setUberPetsPrice(RideCalculator.calculateRidePrice(Product.UberPets, distance));
                    break;
                default:
            }
        }

        rideDataInfoDto.setEta(minEta);
        rideDataInfoDto.setDistance(distance / 1000);
        rideDataInfoDto.setPolyline(calculateRoad.getPolyline());
        return rideDataInfoDto;
    }

    public Set<DriverDto> getDriversInRange(Coordinates coordinates, double radiusInKm) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(driverServiceUrl)
                .queryParam("latitude", coordinates.getLatitude())
                .queryParam("longitude", coordinates.getLongitude())
                .queryParam("radiusInKm", radiusInKm);

        ParameterizedTypeReference<Set<DriverDto>> responseType =
                new ParameterizedTypeReference<>() {
                };

        ResponseEntity<Set<DriverDto>> response = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                null,
                responseType
        );
        return response.getBody();
    }

    private double calculatePrice(long distance, Product product) {
        return switch (product) {
            case UberComfort -> RideCalculator.calculateRidePrice(Product.UberComfort, distance);
            case UberGreen -> RideCalculator.calculateRidePrice(Product.UberGreen, distance);
            case UberX -> RideCalculator.calculateRidePrice(Product.UberX, distance);
            case UberPets -> RideCalculator.calculateRidePrice(Product.UberPets, distance);
        };
    }
}
