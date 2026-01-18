package com.client_app.service;

import com.client_app.exceptions.AuthRuntimeException;
import com.client_app.exceptions.LoginFailedException;
import com.uber.common.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${keycloak.login-server-url}")
    private String keycloakTokenUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final WebClient webClient;

    public TokenResponse login(String username, String password) {
        try {
            return webClient
                    .post()
                    .uri(keycloakTokenUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .bodyValue(buildRequestBody(username, password))
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                throw new LoginFailedException("Niepoprawne dane logowania: " + e.getResponseBodyAsString(), e);
            } else {
                throw new AuthRuntimeException("Błąd podczas logowania: " + e.getStatusCode()
                        + " - " + e.getResponseBodyAsString(), e);
            }
        }
    }

    private MultiValueMap<String, String> buildRequestBody(String username, String password) {
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("username", username);
        map.add("password", password);
        return map;
    }
}
