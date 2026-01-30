package com.client_app.controller;

import com.client_app.model.ride_request.CreateRideRequestCommand;
import com.client_app.service.RideInfoService;
import com.client_app.service.RideRequestService;
import com.uber.common.Coordinates;
import com.uber.common.productSelector.RideDataInfoDto;
import com.uber.common.dto.RideRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/client-app/api/ride-requests")
@RequiredArgsConstructor
@Slf4j
public class RideRequestController {

    private final RideRequestService rideRequestService;
    private final RideInfoService rideInfoService;

    @PostMapping
    public ResponseEntity<RideRequestDto> createRideRequest(@Valid @RequestBody CreateRideRequestCommand command) {
        RideRequestDto rideRequestDto = rideRequestService.createRideRequest(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(rideRequestDto);
    }

    @GetMapping("/info")
    public ResponseEntity<RideDataInfoDto> getRideInfo(
            @RequestParam double pickupLatitude,
            @RequestParam double pickupLongitude,
            @RequestParam double destinationLatitude,
            @RequestParam double destinationLongitude) {

        Coordinates pickupLocation = new Coordinates(pickupLatitude, pickupLongitude);
        Coordinates destinationLocation = new Coordinates(destinationLatitude, destinationLongitude);

        RideDataInfoDto rideDataInfoDto = rideInfoService.getRideInfo(pickupLocation, destinationLocation);
        return ResponseEntity.ok(rideDataInfoDto);
    }
}