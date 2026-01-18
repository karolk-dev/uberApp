package com.driver_app.service;

import com.google.maps.internal.PolylineEncoding;
import com.uber.common.Coordinates;
import com.uber.common.DriverLocationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideSimulatorService {

    private final KafkaTemplate<String, DriverLocationMessage> kafkaTemplate;

    public void startRide(String polyline, String clientUuid) {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        List<Coordinates> decodePolyline = PolylineEncoding.decode(polyline).stream()
                .map(latLng -> new Coordinates(latLng.lat, latLng.lng))
                .toList();
        final Iterator<Coordinates> iterator = decodePolyline.iterator();

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (iterator.hasNext()) {
                Coordinates currentLatLang = iterator.next();
                DriverLocationMessage locationMessage = DriverLocationMessage.builder()
                        .coordinates(currentLatLang)
                        .clientUuid(clientUuid)
                        .build();
                kafkaTemplate.send("driver_position", locationMessage);
                log.info(currentLatLang.getLatitude() + "  " + currentLatLang.getLongitude());

            } else {
                scheduledExecutorService.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
