package com.server_app.service;

import com.server_app.model.Ride;
import com.server_app.model.command.EditRideCommand;
import com.server_app.repository.RideRepository;
import com.uber.common.Coordinates;
import com.uber.common.dto.DriverDto;
import com.uber.common.dto.DriverResponseDto;
import com.uber.common.dto.RideResponseDto;
import com.uber.common.model.RideStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class DriverResponseService {
    private static final long driverFindTimeout = 5 * 60 * 1000;
    private final RideRepository rideRepository;
    private final KafkaTemplate<String, RideResponseDto> kafkaTemplate;
    private final DriverFinder driverFinder;
    private final RideRequestSenderService rideRequestSenderService;
    private final RideService rideService;

    @KafkaListener(topics = "driver_responses", groupId = "server_group")
    public void listenDriverResponses(DriverResponseDto driverResponse) throws Exception {
        DriverDto nearbyDriver;
        log.info("log0");

        Ride ride = rideRepository.findByUuid(driverResponse.getRideUuid()).orElseThrow(() -> new RuntimeException("popraw na customowy wyjatek"));
        long elapsedMillis = System.currentTimeMillis()
                - ride.getSearchStartTime().toInstant().toEpochMilli();
        Coordinates pickupLocation = new Coordinates(ride.getPickupLocationLatitude(), ride.getPickupLocationLongitude());

        if (driverResponse.isAccepted()) {
            log.info("log1");
            if (elapsedMillis > 30000) {
                log.info("uplynelo 30 sek");
                return;
            }
            EditRideCommand command = EditRideCommand.builder()
                    .updatedAt(LocalDateTime.now())
                    .status(RideStatus.IN_PROGRESS).build();
            rideService.editRide(command, driverResponse.getRideUuid());
            log.info("driver " + driverResponse.getDriverUuid() + " " + "zaakceptowal przejazd");
            log.info("aktualizacja ride: " + "driver " + command.getDriverUuid() + " " + "status " + command.getStatus().toString());
            log.info("sprawdzinie danych" + rideRepository.findByUuid(driverResponse.getRideUuid()).get().getStatus());
        } else {
            log.info("driver " + driverResponse.getDriverUuid() + " odrzucil przejazd ");

            if (elapsedMillis > driverFindTimeout) {
                log.info("uplynelo 5 min");
                return;
            }

            nearbyDriver = driverFinder.findNearbyDriver(pickupLocation, 10, driverResponse.getDriverUuid());
            log.info("wybrano nowego drivera " + nearbyDriver.getUuid());
            EditRideCommand editRideCommand = EditRideCommand.builder()
                    .status(RideStatus.REJECTED)
                    .driverUuid(nearbyDriver.getUuid()).build();

            Ride ride1 = rideService.editRide(editRideCommand, driverResponse.getRideUuid());
            log.info("aktualizacja ride " + "status " + editRideCommand.getStatus().toString() + " driver " + editRideCommand.getDriverUuid());

            rideRequestSenderService.sendProposal(rideRequestSenderService.makeProposal(ride1));
        }
        sendRideConfirmation(driverResponse, ride.getClientUuid());
    }

    private void sendRideConfirmation(DriverResponseDto driverResponseDto, String clienUuid) {
        RideResponseDto responseDto = new RideResponseDto(driverResponseDto.getDriverUuid(), driverResponseDto.getRideUuid(),
                driverResponseDto.isAccepted(), clienUuid);

        kafkaTemplate.send("rides_infos", responseDto);
    }
}
