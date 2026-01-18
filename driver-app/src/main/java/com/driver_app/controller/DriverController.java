package com.driver_app.controller;

import com.driver_app.service.*;
import com.uber.common.Coordinates;
import com.uber.common.LoginRequest;
import com.uber.common.TokenResponse;
import com.uber.common.command.CreateDriverCommand;
import com.uber.common.command.UpdateDriverLocationCommand;
import com.uber.common.dto.ChatMessage;
import com.uber.common.dto.DriverDto;
import com.uber.common.dto.RideResponseDto;
import com.uber.common.model.DriverStatusUpdateRequest;
import com.uber.common.model.RideStatus;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/driver-app/api/drivers")
@RequiredArgsConstructor
@Slf4j
public class DriverController {
    private final DriverService driverService;
    private final ProposalStorageService proposalStorageService;
    private final ResponseSenderService responseSenderService;
    private final AuthService authService;
    private final RideSimulatorService rideSimulatorService;
    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;

    @PostMapping("/register")
    public ResponseEntity<DriverDto> registerDriver(@RequestBody CreateDriverCommand command) {
        DriverDto driverDto = driverService.registerDriver(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(driverDto);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(tokenResponse);
    }



    @PutMapping("/{driverUuid}/status")
    public ResponseEntity<Void> updateDriverStatus(
            @PathVariable String driverUuid,
            @RequestBody DriverStatusUpdateRequest request) {
        driverService.updateDriverStatus(driverUuid, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{driverUuid}/location")
    public ResponseEntity<Void> updateDriverLocation(
            @PathVariable String driverUuid,
            @RequestBody UpdateDriverLocationCommand command) {
        driverService.updateDriverLocation(driverUuid, command);
        return ResponseEntity.noContent().build();
    }

    @PermitAll
    @GetMapping("/range")
    public ResponseEntity<Set<DriverDto>> getDriversWithinRadius(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radiusInKm) {
        Coordinates coordinates = new Coordinates();
        coordinates.setLatitude(latitude);
        coordinates.setLongitude(longitude);
        Set<DriverDto> drivers = driverService.getDriversWithinRadius(coordinates, radiusInKm);
        return ResponseEntity.ok(drivers);
    }

    @MessageMapping("/driver/ride/response")
    public void driverRideResponse(RideResponseDto response) {
        responseSenderService.sendResponse(response.getDriverUuid(), response.isAccepted(), response.getRideUuid());
    }

    @PostMapping("/finish/{rideUuid}")
    public ResponseEntity<RideStatus> finishRide(@PathVariable String rideUuid) {
        RideStatus rideStatus = driverService.finishRide(rideUuid);
        return ResponseEntity.ok(rideStatus);
    }

    @PostMapping("/start/{clientUuid}")
    public ResponseEntity<Void> startRide(@RequestBody String polylineToClient, @PathVariable String clientUuid) {
        rideSimulatorService.startRide(polylineToClient, clientUuid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/penalty/{rideUuid}")
    public ResponseEntity<?> penalty(@PathVariable String rideUuid) {
        return ResponseEntity.ok(driverService.penalty(rideUuid));
    }

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage chatMessage) {
        log.info("Wyslano wiadomosc do: " + chatMessage.getRecipient());
        kafkaTemplate.send("chat_driver_to_server", chatMessage);
    }
}