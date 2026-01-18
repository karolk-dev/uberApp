package com.client_app.controller;

import com.client_app.model.client.Client;
import com.client_app.repository.ClientRepository;
import com.client_app.model.client.CreateClientCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
public class ClientControllerIT extends KeycloakTestContainers {

    private final MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    static Keycloak keycloakAdmin;

    @Autowired
    public ClientControllerIT(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Container
    public static final GenericContainer REDIS = new FixedHostPortGenericContainer("redis:latest")
            .withFixedExposedPort(6379, 6379);

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.flyway.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgreSQLContainer::getUsername);
        registry.add("spring.flyway.password", postgreSQLContainer::getPassword);
        registry.add("eureka.client.register-with-eureka", () -> "false");
        registry.add("eureka.client.fetch-registry", () -> "false");
    }

    @Test
    void registerClientIntegrationTest() throws Exception {
        CreateClientCommand command = CreateClientCommand.builder()
                .username("jan")
                .password("asdf")
                .email("jan@gmail.com")
                .build();
        String json = objectMapper.writeValueAsString(command);

        mockMvc.perform(post("/client-app/api/clients/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated());

        keycloakAdmin = KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(keycloak.getAdminUsername())
                .password(keycloak.getAdminPassword())
                .build();

        List<UserRepresentation> users = keycloakAdmin
                .realm("client-app")
                .users()
                .search("jan");
        assertThat(users).isNotEmpty();
        List<Client> clients = clientRepository.findAll();
        assertThat(clients.size()).isEqualTo(1);
    }

    @Test
    void shouldLoginUserSuccessfully() throws Exception {
        createUser();
        String body = """
                {
                  "username": "test",
                  "password": "haslo"
                }
                """;

        mockMvc.perform(post("/client-app/api/clients/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void shouldLoginUserIsUnauthorized() throws Exception {
        String body = """
                
                {
                  "username": "test1",
                  "password": "haslo1"
                }
                """;

        mockMvc.perform(post("/client-app/api/clients/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLoginUserError() throws Exception {
        String body = """
                
                {
                  "username": "test1",
                  "password": "haslo1"
                }
                """;

        mockMvc.perform(post("/client-app/api/clients/loginn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError());
    }

    private void createUser() {
        keycloakAdmin = KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(keycloak.getAdminUsername())
                .password(keycloak.getAdminPassword())
                .build();

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue("haslo");
        cred.setTemporary(false);
        UserRepresentation user = new UserRepresentation();
        user.setUsername("test");
        user.setCredentials(Collections.singletonList(cred));
        user.setEnabled(true);

        Response response1 = keycloakAdmin.realm("client-app").users().create(user);

        if (response1.getStatus() != 201) {
            throw new RuntimeException("Nie udało się utworzyć użytkownika: " + response1.getStatus());
        }
        response1.close();
    }
}
