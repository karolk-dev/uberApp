package com.client_app.service;

import com.uber.common.dto.RideResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverResponseService {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "rides_infos", groupId = "driver_group")
    public void clientListener(RideResponseDto responseDto) {
        log.info("otrzymano info o przejezdzie");

        String clientUuid = responseDto.getClientUuid();
        if (clientUuid != null && !clientUuid.isEmpty()) {
            String destination = "/topic/driver-response/" + clientUuid;
            messagingTemplate.convertAndSend(destination, responseDto);
            log.info("Wysłano powiadomienie do klienta {} na temat {}", clientUuid, destination);
        } else {
            log.error("Brak clientUid w odpowiedzi: {}, responseDto");
        }
    }
}
