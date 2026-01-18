package com.driver_app.service;

import com.uber.common.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "chat_server_to_driver", groupId = "chat-group")
    public void listenChatMessage(ChatMessage chatMessage) {
        String destination = "/topic/chat/" + chatMessage.getRecipient();
        log.info("wiadomosc od " + chatMessage.getSender() + "do: " + chatMessage.getRecipient() + "wiadomosc: " + chatMessage.getContent());
        messagingTemplate.convertAndSend(destination, chatMessage);
    }
}
