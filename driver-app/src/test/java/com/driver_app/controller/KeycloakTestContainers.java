package com.driver_app.controller;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class KeycloakTestContainers {

    static KeycloakContainer keycloak;

    static {
        keycloak = new KeycloakContainer()
                .withRealmImportFile("test-realm.json");
        keycloak.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloak.getAuthServerUrl() + "/realms/driver-app");
        registry.add("keycloak.auth-server-url", keycloak::getAuthServerUrl);
        registry.add("keycloak.realm", () -> "driver-app");
        registry.add("keycloak.resource", () -> "micro-services-api");
        registry.add("keycloak.credentials.secret", () -> "my-test-secret-123");
        registry.add("keycloak.client-secret", () -> "my-test-secret-123");
        registry.add("keycloak.public-client", () -> "false");
        registry.add("keycloak.login-server-url",
                () -> keycloak.getAuthServerUrl() + "/realms/driver-app/protocol/openid-connect/token");

    }

}
