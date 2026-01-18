package com.client_app.controller;

import com.client_app.service.AuthService;
import com.stripe.exception.StripeException;
import com.uber.common.dto.ChatMessage;
import com.uber.common.LoginRequest;
import com.uber.common.TokenResponse;
import com.uber.common.dto.ClientDto;
import com.client_app.model.client.CreateClientCommand;
import com.client_app.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client-app/api/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;
    private final AuthService authService;
    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;

    @PostMapping("/register")
    public ResponseEntity<ClientDto> registerClient(@Valid @RequestBody CreateClientCommand command) throws StripeException {
        ClientDto clientDto = clientService.registerClient(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(clientDto);
    }

    @CrossOrigin(origins = "http://localhost:63342")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/addCard/{uuid}")
    public ResponseEntity<?> addCard(@PathVariable String uuid, @RequestParam String token) throws Exception {
        clientService.addCard(token, uuid);
        return ResponseEntity.ok("ok");
    }

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage chatMessage) {
        log.info("wiadomosc wyslana do " + chatMessage.getRecipient() + " widomosc: " + chatMessage.getContent());
        kafkaTemplate.send("chat_client_to_server", chatMessage);
    }
}
