package com.driver_app.service;

import com.driver_app.repository.DriverRepository;
import com.uber.common.dto.DriverResponseDto;
import com.uber.common.dto.RideProposalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideRequestListenerService {

    private final DriverRepository driverRepository;
    private final KafkaTemplate<String, DriverResponseDto> kafkaTemplate;
    private final DriverService driverService;
    private final ProposalStorageService storageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(topics = "ride_proposal", groupId = "driver_group")
    public void listenRideRequests(RideProposalDto rideProposal) {
        log.info("uuid++++++++++++++++++++" + rideProposal.getDriverUuid());
        log.info("polyline to client++ " + rideProposal.getPolylineToClient());
        String driverUuid = rideProposal.getDriverUuid();
        simpMessagingTemplate.convertAndSend(
                "/topic/driver/" + driverUuid,
                rideProposal
        );
    }

}