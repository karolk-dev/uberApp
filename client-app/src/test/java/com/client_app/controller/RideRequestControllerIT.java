package com.client_app.controller;

import com.client_app.model.client.Client;
import com.client_app.repository.ClientRepository;
import com.uber.common.dto.RideRequestDto;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"ride_requests"})
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RideRequestControllerIT {

    private static final Logger log = LoggerFactory.getLogger(RideRequestControllerIT.class);

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
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.flyway.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgreSQLContainer::getUsername);

        registry.add("spring.flyway.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, RideRequestDto> consumer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRepository clientRepository;

    private String existingClientUuid;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();

        Client client = Client.builder()
                .uuid(UUID.randomUUID().toString())
                .username("testUser")
                .email("test@example.com")
                .role("ROLE_CLIENT")
                .customerId("123")
                .build();


        Client saved = clientRepository.save(client);
        this.existingClientUuid = saved.getUuid();
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker);
        consumer = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(RideRequestDto.class))
                .createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "ride_requests");
    }

    @Test
    void shouldCreateRideRequest() throws Exception {

        String requestBody = String.format("""
                {
                  "clientUuid": "%s",
                  "pickupLocation": {
                    "latitude": 51.1,
                    "longitude": 17.0
                  },
                  "destinationLocation": {
                    "latitude": 52.2,
                    "longitude": 21.0
                  },
                  "product": "UberX"
                }
                """, existingClientUuid);

        mockMvc.perform(post("/client-app/api/ride-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientUuid").value(existingClientUuid))
                .andExpect(jsonPath("$.pickupLocation.latitude").value(51.1))
                .andExpect(jsonPath("$.pickupLocation.longitude").value(17.0))
                .andExpect(jsonPath("$.destinationLocation.latitude").value(52.2))
                .andExpect(jsonPath("$.destinationLocation.longitude").value(21.0))
                .andExpect(jsonPath("$.customerId").value("123"))
                .andExpect(jsonPath("$.status").value("NEW"));

        ConsumerRecords<String, RideRequestDto> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
        assertThat(records.count()).isGreaterThan(0);

        RideRequestDto rideRequestDto = records.records("ride_requests").iterator().next().value();
        assertThat(rideRequestDto.getClientUuid().equals(existingClientUuid));
    }
}
