package com.client_app.service;

import com.client_app.model.ride_request.CreateRideRequestCommand;
import com.client_app.model.ride_request.RideRequest;
import com.client_app.repository.ClientRepository;
import com.uber.common.dto.RideRequestDto;
import com.client_app.model.client.Client;
import com.uber.common.productSelector.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideRequestServiceTest {

    @Mock
    private KafkaTemplate<String, RideRequestDto> kafkaTemplate;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private RideRequestService rideRequestService;

    @Test
    public void createRideRequest_shouldReturnRideRequestDto_andSendKafkaMessage() {
        CreateRideRequestCommand command = CreateRideRequestCommand.builder()
                .clientUuid("client-uuid-1")
                .pickupLocation(new com.uber.common.Coordinates(1.0, 2.0))
                .destinationLocation(new com.uber.common.Coordinates(3.0, 4.0))
                .product(Product.valueOf("UberX"))
                .build();

        Client dummyClient = Client.builder()
                .customerId(String.valueOf(12345L))
                .build();
        when(clientRepository.findByUuid("client-uuid-1")).thenReturn(Optional.of(dummyClient));

        when(customerService.retrievePaymentMethods("12345")).thenReturn("pm-6789");

        RideRequestDto expectedDto = new RideRequestDto();
        when(modelMapper.map(any(RideRequest.class), eq(RideRequestDto.class))).thenReturn(expectedDto);

        RideRequestDto result = rideRequestService.createRideRequest(command);

        assertEquals(expectedDto, result);

        verify(kafkaTemplate).send("ride_requests", expectedDto);

        verify(clientRepository).findByUuid("client-uuid-1");
        verify(customerService).retrievePaymentMethods("12345");
    }
}
