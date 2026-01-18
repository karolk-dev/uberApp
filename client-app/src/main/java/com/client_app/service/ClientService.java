package com.client_app.service;

import com.client_app.exceptions.ClientRegistrationException;
import com.client_app.model.client.CreateClientCommand;
import com.stripe.model.PaymentMethod;
import com.uber.common.dto.ClientDto;
import com.client_app.model.client.Client;
import com.client_app.exceptions.DuplicateClientException;
import com.client_app.repository.ClientRepository;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.keycloak.admin.client.CreatedResponseUtil.getCreatedId;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;
    private final CustomerService customerService;
    private final Keycloak keycloak;
    @Value("${keycloak.realm}")
    private String realm;


    public ClientDto registerClient(CreateClientCommand command) {
        if (clientRepository.existsByUsername(command.getUsername())) {
            throw new DuplicateClientException("Username " + command.getUsername() + " is already taken");
        }
        log.info("Rozpoczynam rejestrację klienta o username: {}", command.getUsername());

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(command.getUsername());
        userRepresentation.setEmail(command.getEmail());
        userRepresentation.setEnabled(true);
        log.info("Przygotowano UserRepresentation dla Keycloak: username={}, email={}",
                command.getUsername(), command.getEmail());

        Response response = keycloak.realm(realm).users().create(userRepresentation);
        log.info("Otrzymano odpowiedź z Keycloak przy tworzeniu użytkownika. Status: {}", response.getStatus());

        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            String errorMsg = "Nie udało się utworzyć użytkownika w Keycloak. Status: "
                    + response.getStatus();
            log.error(errorMsg);
            response.close();
            throw new ClientRegistrationException(errorMsg);
        }

        String userId = getCreatedId(response);
        response.close();
        log.info("Użytkownik w Keycloak utworzony pomyślnie, userId: {}", userId);

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(command.getPassword());
        log.info("Resetowanie hasła dla użytkownika o userId: {}", userId);

        try {
            keycloak.realm(realm).users().get(userId).resetPassword(passwordCred);
            log.info("Hasło zostało ustawione pomyślnie dla userId: {}", userId);
        } catch (Exception e) {
            String errorMsg = "Błąd podczas ustawiania hasła dla userId: " + userId;
            log.error(errorMsg, e);
            throw new ClientRegistrationException(errorMsg, e);
        }

        Client client = createClient(command, userId);
        client = clientRepository.save(client);
        log.info("Klient został zarejestrowany pomyślnie. userId: {}, customerId: {}",
                userId, client.getCustomerId());

        return modelMapper.map(client, ClientDto.class);
    }

    public Client createClient(CreateClientCommand command, String uuid) {
        return Client.builder()
                .uuid(uuid)
                .username(command.getUsername())
                .email(command.getEmail())
                .role("ROLE_CLIENT")
                .customerId(customerService.createCustomer(command.getEmail()).getId())
                .build();
    }

    public void addCard(String token, String clientUuid) throws Exception {
        PaymentMethod paymentMethod = customerService.createPaymentMethod(token);
        customerService.attachPaymentMethodToCustomer(paymentMethod.getId(), clientRepository.findByUuid(clientUuid).get().getCustomerId());
    }
}