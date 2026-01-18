package com.server_app.service;

import com.server_app.model.Ride;
import com.server_app.model.command.EditRideCommand;
import com.server_app.repository.RideRepository;
import com.stripe.model.PaymentIntent;
import com.uber.common.model.RideStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PaymentService paymentService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RideService rideService;

    @Test
    void testGetRideStatus_found() {
        Long rideId = 1L;
        Ride ride = new Ride();
        ride.setStatus(RideStatus.IN_PROGRESS);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        RideStatus status = rideService.getRideStatus(rideId);
        assertEquals(RideStatus.IN_PROGRESS, status);
    }

    @Test
    void testEditRide_success() {
        String uuid = "ride-uuid";
        Ride ride = new Ride();
        ride.setUuid(uuid);
        when(rideRepository.findByUuid(uuid)).thenReturn(Optional.of(ride));

        EditRideCommand command = new EditRideCommand();
        command.setClientUuid("client-uuid");
        command.setUuid("new-ride-uuid");
        command.setDriverUuid("driver-uuid");
        command.setPickupLocationLatitude(10.0);
        command.setPickupLocationLongitude(20.0);
        command.setDestinationLatitude(30.0);
        command.setDestinationLongitude(40.0);
        command.setUpdatedAt(LocalDateTime.of(2023, 1, 1, 10, 0));
        command.setStatus(RideStatus.IN_PROGRESS);
        command.setPenaltyAmount(500);

        Ride editedRide = rideService.editRide(command, uuid);

        assertEquals("client-uuid", editedRide.getClientUuid());
        assertEquals("new-ride-uuid", editedRide.getUuid());
        assertEquals("driver-uuid", editedRide.getDriverUuid());
        assertEquals(10.0, editedRide.getPickupLocationLatitude());
        assertEquals(20.0, editedRide.getPickupLocationLongitude());
        assertEquals(30.0, editedRide.getDestinationLatitude());
        assertEquals(40.0, editedRide.getDestinationLongitude());
        assertEquals(RideStatus.IN_PROGRESS, editedRide.getStatus());
        assertEquals(500, editedRide.getPenaltyAmount());
        assertNotNull(editedRide.getUpdatedAt());
    }

    @Test
    void testEditRide_notFound() {
        String uuid = "nonexistent-uuid";
        when(rideRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        EditRideCommand command = new EditRideCommand();


        assertThrows(RuntimeException.class, () -> rideService.editRide(command, uuid));
    }

    @Test
    void testFinishRide_success() throws Exception {
        String rideUuid = "ride-uuid";
        Ride ride = new Ride();
        ride.setUuid(rideUuid);
        ride.setPaymentIntendId("pi_123");
        ride.setAmount(100L);
        ride.setPenaltyAmount(20L);
        ride.setStatus(RideStatus.IN_PROGRESS);

        when(rideRepository.findByUuid(rideUuid)).thenReturn(Optional.of(ride));


        Ride finishedRide = rideService.finishRide(rideUuid);
//        assertEquals(RideStatus.IN_PROGRESS, finishedRide.getStatus());
        verify(rideRepository).save(ride);
    }


}
