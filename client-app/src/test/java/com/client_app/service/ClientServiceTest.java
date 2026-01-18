package com.client_app.service;

import com.client_app.exceptions.ClientRegistrationException;
import com.client_app.exceptions.DuplicateClientException;
import com.client_app.model.client.Client;
import com.client_app.model.client.CreateClientCommand;
import com.client_app.repository.ClientRepository;
import com.stripe.exception.StripeException;
import com.uber.common.dto.ClientDto;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private CustomerService customerService;

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @InjectMocks
    private ClientService clientService;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(clientService, "realm", "testRealm");
        lenient().when(keycloak.realm("testRealm")).thenReturn(realmResource);
        lenient().when(realmResource.users()).thenReturn(usersResource);
    }

    @Test
    public void testRegisterClientWhenUsernameExists() {
        // given
        CreateClientCommand command = CreateClientCommand.builder()
                .username("existingUser")
                .build();
        when(clientRepository.existsByUsername("existingUser")).thenReturn(true);
        // when & then
        assertThrows(DuplicateClientException.class, () -> clientService.registerClient(command));
        verify(clientRepository).existsByUsername("existingUser");
    }

    @Test
    public void testRegisterClientKeycloakFailure() {
        // given
        CreateClientCommand command = CreateClientCommand.builder()
                .username("newUser")
                .email("newUser@example.com")
                .password("password")
                .build();
        when(clientRepository.existsByUsername("newUser")).thenReturn(false);

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(400);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        // when & then
        ClientRegistrationException ex =
                assertThrows(ClientRegistrationException.class, () -> clientService.registerClient(command));
        assertTrue(ex.getMessage().contains("Nie udało się utworzyć użytkownika"),
                "Komunikat wyjątku powinien zawierać informację o nieudanej rejestracji");

        verify(usersResource).create(any(UserRepresentation.class));
    }

    @Test
    public void testRegisterClientPasswordResetFailure() {
        // given
        CreateClientCommand command = CreateClientCommand.builder()
                .username("newUser")
                .email("newUser@example.com")
                .password("password")
                .build();
        when(clientRepository.existsByUsername("newUser")).thenReturn(false);

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(201);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

        try (MockedStatic<CreatedResponseUtil> mockedStatic = mockStatic(CreatedResponseUtil.class)) {
            mockedStatic.when(() -> CreatedResponseUtil.getCreatedId(response)).thenReturn("user123");
            when(usersResource.get("user123")).thenReturn(userResource);

            doThrow(new RuntimeException("Błąd Keycloak przy ustawianiu hasła"))
                    .when(userResource).resetPassword(any(CredentialRepresentation.class));

            // when & then
            ClientRegistrationException ex =
                    assertThrows(ClientRegistrationException.class, () -> clientService.registerClient(command));
            assertTrue(ex.getMessage().contains("Błąd podczas ustawiania hasła"),
                    "Komunikat wyjątku powinien zawierać informację o błędzie przy resetowaniu hasła");
        }
    }

    @Test
    public void testRegisterClientSuccess() {
        // given
        CreateClientCommand command = CreateClientCommand.builder()
                .username("newUser")
                .email("newUser@example.com")
                .password("password")
                .build();
        when(clientRepository.existsByUsername("newUser")).thenReturn(false);
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(201);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        try (MockedStatic<CreatedResponseUtil> mockedStatic = mockStatic(CreatedResponseUtil.class)) {
            mockedStatic.when(() -> CreatedResponseUtil.getCreatedId(response)).thenReturn("user123");
            when(usersResource.get("user123")).thenReturn(userResource);
            doNothing().when(userResource).resetPassword(any(CredentialRepresentation.class));
            Customer stripeCustomer = new Customer();
            stripeCustomer.setId("cust123");
            when(customerService.createCustomer("newUser@example.com")).thenReturn(stripeCustomer);
            Client client = Client.builder()
                    .uuid("user123")
                    .username("newUser")
                    .email("newUser@example.com")
                    .role("ROLE_CLIENT")
                    .customerId("cust123")
                    .build();
            when(clientRepository.save(any(Client.class))).thenReturn(client);
            ClientDto clientDto = ClientDto.builder()
                    .uuid("user123")
                    .username("newUser")
                    .email("newUser@example.com")
                    .build();
            when(modelMapper.map(client, ClientDto.class)).thenReturn(clientDto);
            // when
            ClientDto result = clientService.registerClient(command);
            // then
            assertNotNull(result);
            assertEquals("newUser", result.getUsername());
            assertEquals("newUser@example.com", result.getEmail());
            assertEquals("user123", result.getUuid());
        }
        verify(usersResource).create(any(UserRepresentation.class));
        verify(userResource).resetPassword(any(CredentialRepresentation.class));
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    public void testAddCardSuccess() throws Exception {
        // given
        String token = "card_token";
        String clientUuid = "client123";
        Client client = Client.builder()
                .customerId("cust123")
                .build();
        when(clientRepository.findByUuid(clientUuid)).thenReturn(Optional.of(client));
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId("pm_123");
        when(customerService.createPaymentMethod(token)).thenReturn(paymentMethod);
        when(customerService.attachPaymentMethodToCustomer("pm_123", "cust123")).thenReturn(null);
        // when
        clientService.addCard(token, clientUuid);
        // then
        verify(customerService).createPaymentMethod(token);
        verify(customerService).attachPaymentMethodToCustomer("pm_123", "cust123");
    }

    @Test
    public void testAddCardClientNotFound() throws StripeException {
        // given
        String token = "card_token";
        String clientUuid = "nonexistent";
        when(clientRepository.findByUuid(clientUuid)).thenReturn(Optional.empty());
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId("pm_123");
        when(customerService.createPaymentMethod(token)).thenReturn(paymentMethod);
        // when & then
        assertThrows(NoSuchElementException.class, () -> clientService.addCard(token, clientUuid));
    }
}
