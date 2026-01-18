package com.server_app.service;

import com.server_app.model.Ride;
import com.server_app.repository.RideRepository;
import com.server_app.routing.GoogleRoutesService;
import com.server_app.routing.RouteInfo;
import com.stripe.model.PaymentIntent;
import com.uber.common.dto.DriverDto;
import com.uber.common.dto.RideProposalDto;
import com.uber.common.dto.RideRequestDto;
import com.uber.common.model.Currency;
import com.uber.common.model.PaymentType;
import com.uber.common.model.RideStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientRideRequestService {

    private final RideService rideService;
    private final RideRepository rideRepository;
    private final KafkaTemplate<String, RideProposalDto> kafkaTemplate;
    private final DriverFinder finder;
    private final int radius = 10;
    private final RideRequestSenderService rideRequestSenderService;
    private final PaymentService paymentService;
    private final GoogleRoutesService googleRoutesService;

    @KafkaListener(topics = "ride_requests", groupId = "client-app-group")
    public void listenRideRequests(RideRequestDto rideRequestDto) throws Exception {
        RideProposalDto proposalDto = new RideProposalDto();
        DriverDto driverDto1 = finder.findNearbyDriver(rideRequestDto.getPickupLocation(), radius, null);
        RouteInfo routeInfo = googleRoutesService.getRouteInfo(rideRequestDto.getPickupLocation(), rideRequestDto.getDestinationLocation());
        RouteInfo infoForDriver = googleRoutesService.getRouteInfo(rideRequestDto.getPickupLocation(), driverDto1.getCoordinates());

        long distance = routeInfo.getDistanceInMeters();
        long amount = (long) RideCalculator.calculateRidePrice(rideRequestDto.getProduct(), distance) * 100;

        PaymentIntent paymentIntent = paymentService.createPaymentIntent(
                rideRequestDto.getCustomerId(),
                amount,
                Currency.PLN.name(),
                rideRequestDto.getPaymentMethodId()
        );

        Ride ride = Ride.builder()
                .clientUuid(rideRequestDto.getClientUuid())
                .driverUuid(driverDto1.getUuid())
                .uuid(UUID.randomUUID().toString())
                .pickupLocationLongitude(rideRequestDto.getPickupLocation().getLongitude())
                .pickupLocationLatitude(rideRequestDto.getPickupLocation().getLatitude())
                .destinationLongitude(rideRequestDto.getDestinationLocation().getLongitude())
                .destinationLatitude(rideRequestDto.getDestinationLocation().getLatitude())
                .status(RideStatus.PENDING)
                .searchStartTime(OffsetDateTime.now())
                .product(rideRequestDto.getProduct())
                .createdAt(LocalDateTime.now())
                .paymentIntendId(paymentIntent.getId())
                .customerId(rideRequestDto.getCustomerId())
                .amount(amount)
                .currency(String.valueOf(Currency.PLN))
                .paymentType(PaymentType.CARD)
                .polyline(routeInfo.getPolyline())
                .polylineToClient(infoForDriver.getPolyline())
                .build();

        rideRepository.save(ride);
        rideRequestSenderService.sendProposal(rideRequestSenderService.makeProposal(ride));
    }

    public void sendProposal(RideProposalDto rideProposalDto) {
        kafkaTemplate.send("ride_proposal", rideProposalDto);
    }
}