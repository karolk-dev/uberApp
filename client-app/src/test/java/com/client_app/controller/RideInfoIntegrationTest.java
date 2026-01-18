package com.client_app.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
public class RideInfoIntegrationTest extends DatabaseContainer {

    @Autowired
    private MockMvc mockMvc;

    @LocalServerPort
    private int port;

    @BeforeAll
    static void setup() {
        WireMock.configureFor(wireMockServer.getPort());
    }

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("server.service.ride-info.url", () -> wireMockServer.baseUrl() + "/client-app/api/ride-requests/info");
    }

    @Test
    void shouldGetRideInfoFromExternalService() throws Exception {
        stubFor(WireMock.get(urlPathEqualTo("/client-app/api/ride-requests/info"))
                .withQueryParam("pickupLatitude", equalTo("52.1"))
                .withQueryParam("pickupLongitude", equalTo("21.0"))
                .withQueryParam("destinationLatitude", equalTo("52.2"))
                .withQueryParam("destinationLongitude", equalTo("21.1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                      "uberXPrice": 12.5,
                      "uberComfortPrice": 17.0,
                      "uberPetsPrice": 20.0,
                      "uberGreenPrice": 15.0,
                      "eta": 5,
                      "distance": 4300,
                      "polyline": "abc123"
                    }
                    """)));

        mockMvc.perform(get("/client-app/api/ride-requests/info")
                        .param("pickupLatitude", "52.1")
                        .param("pickupLongitude", "21.0")
                        .param("destinationLatitude", "52.2")
                        .param("destinationLongitude", "21.1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uberXPrice").value(12.5))
                .andExpect(jsonPath("$.uberComfortPrice").value(17.0))
                .andExpect(jsonPath("$.eta").value(5))
                .andExpect(jsonPath("$.distance").value(4300))
                .andExpect(jsonPath("$.polyline").value("abc123"));


    }
}
