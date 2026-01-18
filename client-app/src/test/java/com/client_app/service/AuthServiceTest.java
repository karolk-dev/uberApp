package com.client_app.service;

import com.uber.common.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "keycloakTokenUrl", "http://localhost/token");
        ReflectionTestUtils.setField(authService, "clientId", "testClientId");
        ReflectionTestUtils.setField(authService, "clientSecret", "testClientSecret");
    }

    @Test
    void login_shouldReturnTokenResponse_whenCredentialsAreValid() {
        // Given
        String username = "user";
        String password = "pass";
        TokenResponse expectedResponse = TokenResponse.builder()
                .access_token("test_token")
                .token_type("Bearer")
                .expires_in(3600L)
                .build();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("http://localhost/token")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any(MultiValueMap.class)))
                .thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenResponse.class)).thenReturn(Mono.just(expectedResponse));

        // When
        TokenResponse actualResponse = authService.login(username, password);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);
        verify(requestBodyUriSpec).bodyValue(captor.capture());
        MultiValueMap<String, String> requestBody = captor.getValue();
        assertEquals("password", requestBody.getFirst("grant_type"));
        assertEquals("testClientId", requestBody.getFirst("client_id"));
        assertEquals("testClientSecret", requestBody.getFirst("client_secret"));
        assertEquals(username, requestBody.getFirst("username"));
        assertEquals(password, requestBody.getFirst("password"));
    }

    @Test
    void login_shouldThrowAuthRuntimeException_whenServerErrorOccurs() {
        // Given
        String username = "user";
        String password = "pass";

        WebClientResponseException exception = WebClientResponseException.create(
                500,
                "Internal Server Error",
                HttpHeaders.EMPTY,
                "Server error".getBytes(),
                null
        );

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("http://localhost/token")).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE))
                .thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any(MultiValueMap.class)))
                .thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TokenResponse.class)).thenReturn(Mono.error(exception));

        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> authService.login(username, password));
        String message = thrown.getMessage();
        assertTrue(message.contains("Błąd podczas logowania:"), "Brak kluczowego fragmentu komunikatu");
        assertTrue(message.contains("500"), "Komunikat nie zawiera kodu 500");
        assertTrue(message.contains("Server error"), "Komunikat nie zawiera opisu błędu");
    }
}
