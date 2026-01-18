package com.server_app.service;

import com.server_app.repository.RideRepository;
import com.uber.common.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageProcessorService {

    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;
    private final RideRepository rideRepository;

    @KafkaListener(topics = "chat_driver_to_server", groupId = "server_group")
    public void listenChatMessage(ChatMessage chatMessage) {

        Instant startRide = rideRepository.findByUuid(chatMessage.getRideUuid()).orElseThrow().getUpdatedAt().toInstant(ZoneOffset.UTC);
        Instant messageTimeStamp = chatMessage.getTimestamp();
        long hoursDifference = Duration.between(startRide, messageTimeStamp).toHours();
        if (Math.abs(hoursDifference) > 48) {
            log.info("przekroczono limit 48h");
        } else {
            kafkaTemplate.send("chat_server_to_client", chatMessage);
        }
    }

    @KafkaListener(topics = "chat_client_to_server", groupId = "server_group")
    public void listenChatMessage2(ChatMessage chatMessage) {
        log.info(chatMessage.getRecipient());
        log.info(chatMessage.getContent());
        kafkaTemplate.send("chat_server_to_driver", chatMessage);
    }

    private void chatProcessor(ChatMessage chatMessage) {
        rideRepository.findByUuid(chatMessage.getRideUuid()).orElseThrow().getUpdatedAt();
        LocalDateTime localDateTime = rideRepository.findByUuid(chatMessage.getRideUuid()).orElseThrow().getUpdatedAt();
    }
}
