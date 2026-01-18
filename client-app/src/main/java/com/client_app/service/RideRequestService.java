package com.client_app.service;

import com.client_app.model.ride_request.CreateRideRequestCommand;
import com.client_app.model.ride_request.RideRequest;
import com.client_app.repository.ClientRepository;
import com.uber.common.dto.RideRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideRequestService {

    private final KafkaTemplate<String, RideRequestDto> kafkaTemplate;
    private final ModelMapper modelMapper;
    private final ClientRepository clientRepository;
    private final CustomerService customerService;

    public RideRequestDto createRideRequest(CreateRideRequestCommand command) {
        String clientUuid = command.getClientUuid();
        String clientCustomerId = String.valueOf(clientRepository.findByUuid(command.getClientUuid())
                .orElseThrow()
                .getCustomerId());
        String paymentMethodId = customerService.retrievePaymentMethods(clientCustomerId);
        log.info("customerId " + clientCustomerId);
        log.info("paymentMethodId " + paymentMethodId);

        RideRequest rideRequest = RideRequest.builder()
                .clientUuid(clientUuid)
                .pickupLocation(command.getPickupLocation())
                .destinationLocation(command.getDestinationLocation())
                .status("NEW")
                .product(command.getProduct())
                .customerId(clientCustomerId)
                .paymentMethodId(paymentMethodId)
                .build();

        RideRequestDto rideRequestDto = modelMapper.map(rideRequest, RideRequestDto.class);
        sendRideRequestDto(rideRequestDto);
        return rideRequestDto;
    }

    private void sendRideRequestDto(RideRequestDto rideRequestDto) {
        log.info("wyslano zadanie do serwera");
        kafkaTemplate.send("ride_requests", rideRequestDto);
    }
}