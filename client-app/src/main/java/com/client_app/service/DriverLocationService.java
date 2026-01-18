package com.client_app.service;

import com.uber.common.DriverLocationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverLocationService {
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "driver_position", groupId = "client_group")
    public void listenClientLocation(DriverLocationMessage locationMessage) {
        log.info("odebrano " + locationMessage.getCoordinates().getLongitude() + "  " + locationMessage.getCoordinates().getLatitude()
        + " " + locationMessage.getClientUuid());
        String destination = "/topic/driver-location/" + locationMessage.getClientUuid();
        messagingTemplate.convertAndSend(destination, locationMessage.getCoordinates());
        log.info("wyslano do " + destination);
    }

}
