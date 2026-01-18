package com.driver_app.controller;

import com.driver_app.exceptions.UserCreationException;
import com.driver_app.model.Driver;
import com.driver_app.repository.DriverRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.uber.common.command.CreateDriverCommand;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.hamcrest.Matchers.hasSize;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import org.keycloak.admin.client.Keycloak;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@WireMockTest
@ExtendWith(WireMockExtension.class)
public class DriverControllerIntegrationLoginRegisterTest extends KeycloakTestContainers {

    private final MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DriverRepository driverRepository;

    static Keycloak keycloakAdmin;

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().port(8089))
            .build();

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    public DriverControllerIntegrationLoginRegisterTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

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
    void registerDriverIntegrationTest() throws Exception {
        assertThat(driverRepository.findAll().size()).isEqualTo(0);

        CreateDriverCommand driverCommand = CreateDriverCommand.builder()
                .name("jan")
                .nip("7282830956")
                .password("asdf")
                .build();
        String json = objectMapper.writeValueAsString(driverCommand);

        stubFor(get(urlPathMatching("/api/ceidg/7282830956"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"id\": \"1\",\n" +
                                "  \"nazwa\": \"Test Company\",\n" +
                                "  \"adresDzialalnosci\": {\n" +
                                "      \"ulica\": \"Test Street\",\n" +
                                "      \"miasto\": \"Test City\",\n" +
                                "      \"kod\": \"00-000\",\n" +
                                "      \"kraj\": \"Poland\"\n" +
                                "  },\n" +
                                "  \"wlasciciel\": {\n" +
                                "      \"imie\": \"Jan\",\n" +
                                "      \"nazwisko\": \"Kowalski\"\n" +
                                "  },\n" +
                                "  \"dataRozpoczecia\": \"2020-01-01\",\n" +
                                "  \"status\": \"active\",\n" +
                                "  \"link\": \"http://example.com\"\n" +
                                "}")
                        .withStatus(200)));

        mockMvc.perform(post("/driver-app/api/drivers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        List<Driver> drivers = driverRepository.findAll();
        assertThat(drivers.size()).isEqualTo(1);


    }

    @Test
    void shouldLoginUserSuccessfully() throws Exception {
        createUser();
        String body = """
                {
                  "username": "testuser43",
                  "password": "testpass"
                }
                """;

        mockMvc.perform(post("/driver-app/api/drivers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
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
        cred.setValue("testpass");
        cred.setTemporary(false);


        UserRepresentation user = new UserRepresentation();
        user.setUsername("testuser43");
        user.setCredentials(Collections.singletonList(cred));
        user.setEnabled(true);

        Response response1 = keycloakAdmin.realm("driver-app").users().create(user);


        if (response1.getStatus() != 201) {
            throw new UserCreationException("Nie udało się utworzyć użytkownika: " + response1.getStatus());
        }
        response1.close();
    }






}
