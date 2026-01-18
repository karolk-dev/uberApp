package com.driver_app.service;

import com.uber.common.DriverLocationMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RideSimulatorServiceTest {

    @Mock
    private KafkaTemplate<String, DriverLocationMessage> kafkaTemplate;

    @InjectMocks
    private RideSimulatorService rideSimulatorService;

    @Test
    public void testStartRideSuccess() throws InterruptedException {
        // given
        String polyline = "_p~iF~ps|U";
        String clientUuid = "client-123";

        // when
        rideSimulatorService.startRide(polyline, clientUuid);
        Thread.sleep(2000);

        // then
        verify(kafkaTemplate, atLeastOnce()).send(eq("driver_position"), any(DriverLocationMessage.class));
    }

    @Test
    public void testStartRideKafkaException() throws InterruptedException {
        // given
        String polyline = "_p~iF~ps|U";
        String clientUuid = "client-123";
        doThrow(new RuntimeException("Kafka error"))
                .when(kafkaTemplate).send(eq("driver_position"), any(DriverLocationMessage.class));

        // when
        rideSimulatorService.startRide(polyline, clientUuid);
        Thread.sleep(2000);

        // then
        verify(kafkaTemplate, atLeastOnce()).send(eq("driver_position"), any(DriverLocationMessage.class));
    }
}
