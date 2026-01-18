package com.server_app.config;

import com.uber.common.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "chat-messages", groupId = "chat-service")
    public void consume(ChatMessageDto chatMessageDto) {
        log.info("Received chat message event: {}", chatMessageDto);

        messagingTemplate.convertAndSend("/topic/chat/" + chatMessageDto.getRideUuid(), chatMessageDto);
    }
}