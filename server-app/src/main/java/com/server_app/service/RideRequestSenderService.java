package com.server_app.service;

import com.server_app.model.Ride;
import com.uber.common.dto.RideProposalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideRequestSenderService {

    private final KafkaTemplate<String, RideProposalDto> kafkaTemplate;

    public RideProposalDto makeProposal(Ride ride) {
        log.info("polilineToClient " + ride.getPolylineToClient());

        return RideProposalDto.builder()
                .pickupLocationLongitude(ride.getPickupLocationLongitude())
                .pickupLocationLatitude(ride.getPickupLocationLatitude())
                .destinationLongitude(ride.getDestinationLongitude())
                .destinationLatitude(ride.getDestinationLatitude())
                .rideUuid(ride.getUuid())
                .driverUuid(ride.getDriverUuid())
                .polyline(ride.getPolyline())
                .polylineToClient(ride.getPolylineToClient())
                .clientUuid(ride.getClientUuid())
                .build();
    }

    public void sendProposal(RideProposalDto rideProposalDto) {
        kafkaTemplate.send("ride_proposal", rideProposalDto);
        log.info("wyslano propozycje do: " + rideProposalDto.getDriverUuid());
    }
}
