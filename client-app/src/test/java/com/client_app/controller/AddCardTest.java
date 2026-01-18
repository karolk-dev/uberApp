package com.client_app.controller;

import com.client_app.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class AddCardTest extends DatabaseContainer {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Test
    void testAddCardSuccess() throws Exception {
        String clientUuid = "1234-uuid";
        String token = "test-token";

        doNothing().when(clientService).addCard(token, clientUuid);

        mockMvc.perform(post("/client-app/api/clients/addCard/{uuid}", clientUuid)
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }
}
