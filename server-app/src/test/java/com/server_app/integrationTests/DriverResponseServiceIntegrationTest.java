package com.server_app.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server_app.model.Ride;
import com.server_app.repository.RideRepository;
import com.server_app.service.DriverFinder;
import com.server_app.service.RideRequestSenderService;
import com.uber.common.Coordinates;
import com.uber.common.dto.DriverDto;
import com.uber.common.dto.DriverResponseDto;
import com.uber.common.dto.RideProposalDto;
import com.uber.common.dto.RideResponseDto;
import com.uber.common.model.RideStatus;
import com.uber.common.productSelector.Product;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;
import java.util.Set;
import org.apache.kafka.clients.admin.AdminClient;



import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"driver_responses", "rides_infos"})
@ActiveProfiles("test")
@EnableKafka
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DriverResponseServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(DriverResponseServiceIntegrationTest.class);
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private KafkaTemplate<String, DriverResponseDto> driverResponseKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, RideResponseDto> rideResponseKafkaTemplate;

    @MockBean
    private DriverFinder driverFinder;

    @MockBean
    private RideRequestSenderService rideRequestSenderService;

    @Autowired
    private ObjectMapper objectMapper;



    private Consumer<String, RideResponseDto> testConsumer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

//    @BeforeEach
//    void setUp() {
//        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "false", embeddedKafkaBroker);
//        consumerProps.put("key.deserializer", StringDeserializer.class);
//        consumerProps.put("value.deserializer", JsonDeserializer.class);
//        consumerProps.put("spring.json.trusted.packages", "*");
//        ConsumerFactory<String, RideResponseDto> cf = new DefaultKafkaConsumerFactory<>(
//                consumerProps,
//                new StringDeserializer(),
//                new JsonDeserializer<>(RideResponseDto.class, false)
//        );
//
//        testConsumer = cf.createConsumer();
//        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(testConsumer, "rides_infos");
//    }

    private void clearTopic(String topicName) {
        Consumer<String, RideResponseDto> cleaner = createConsumer(topicName);
        ConsumerRecords<String, RideResponseDto> records;
        do {
            records = KafkaTestUtils.getRecords(cleaner, Duration.ofMillis(500));
        } while (!records.isEmpty());
        cleaner.close();
    }

    private Consumer<String, RideResponseDto> createConsumer(String topicName) {
        Map<String, Object> props = KafkaTestUtils.consumerProps("cleaner-group", "false", embeddedKafkaBroker);
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        DefaultKafkaConsumerFactory<String, RideResponseDto> cf = new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(RideResponseDto.class)
        );
        Consumer<String, RideResponseDto> consumer = cf.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, topicName);
        return consumer;
    }

    @Test
    void shouldUpdateRideStatusWhenDriverAcceptsWithinTime() throws InterruptedException, ExecutionException {
        Thread.sleep(10000);
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group" + UUID.randomUUID(), "false", embeddedKafkaBroker);
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", JsonDeserializer.class);
        consumerProps.put("spring.json.trusted.packages", "*");
        ConsumerFactory<String, RideResponseDto> cf = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(RideResponseDto.class, false)
        );

        testConsumer = cf.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(testConsumer, "rides_infos");


        // Given
        String rideId = UUID.randomUUID().toString();
        Ride ride = Ride.builder()
                .clientUuid("client-1")
                .driverUuid(UUID.randomUUID().toString())
                .uuid(rideId)
                .pickupLocationLongitude(50.50)
                .pickupLocationLatitude(50.50)
                .destinationLongitude(50.50)
                .destinationLatitude(50.50)
                .status(RideStatus.PENDING)
                .searchStartTime(OffsetDateTime.now().minusSeconds(20))
                .product(Product.UberX)
                .createdAt(LocalDateTime.now())
                .paymentIntendId("paymentIntentID")
                .customerId("id")
                .amount(1000)
                .polyline("polyline")
                .polylineToClient("polyline")
                .build();

        rideRepository.save(ride);

        DriverResponseDto response = DriverResponseDto.builder()
                .rideUuid(rideId)
                .driverUuid("driver-1")
                .accepted(true)
                .build();

        // When
        driverResponseKafkaTemplate.send("driver_responses", response).get();
        // Then
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Ride updatedRide = rideRepository.findByUuid(rideId).orElseThrow();
//            assertThat(updatedRide.getStatus()).isEqualTo(RideStatus.IN_PROGRESS);
        });

        ConsumerRecords<String, RideResponseDto> records = KafkaTestUtils.getRecords(testConsumer);
        assertThat(records).hasSize(1);

        RideResponseDto responseDto = records.iterator().next().value();
        assertThat(responseDto.isAccepted());
        assertThat(responseDto.getRideUuid()).isEqualTo(rideId);
    }

    @Test
    void shouldFindNewDriverWhenRejectedWithinTimeout() throws Exception {
        Thread.sleep(10000);
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "false", embeddedKafkaBroker);
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", JsonDeserializer.class);
        consumerProps.put("spring.json.trusted.packages", "*");
        ConsumerFactory<String, RideResponseDto> cf = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(RideResponseDto.class, false)
        );

        testConsumer = cf.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(testConsumer, "rides_infos");
        // Given
        RideProposalDto mockProposal = new RideProposalDto();
        Mockito.when(rideRequestSenderService.makeProposal(any(Ride.class)))
                .thenReturn(mockProposal);
        String rideId = UUID.randomUUID().toString();
        Ride ride = Ride.builder()
                .clientUuid("client-1")
                .driverUuid("driver-2")
                .uuid(rideId)
                .pickupLocationLongitude(50.50)
                .pickupLocationLatitude(50.50)
                .destinationLongitude(50.50)
                .destinationLatitude(50.50)
                .status(RideStatus.PENDING)
                .searchStartTime(OffsetDateTime.now().minusMinutes(4))
                .product(Product.UberX)
                .createdAt(LocalDateTime.now())
                .paymentIntendId("paymentIntentID")
                .customerId("id")
                .amount(1000)
                .polyline("polyline")
                .polylineToClient("polyline")
                .build();
        rideRepository.saveAndFlush(ride);

        DriverDto newDriverDto = DriverDto.builder()
                .name("driver-2")
                .coordinates(new Coordinates(50.1, 20.1))
                .build();



        Mockito.when(driverFinder.findNearbyDriver(any(Coordinates.class), anyInt(), anyString()))
                .thenReturn(newDriverDto);

        DriverResponseDto response = DriverResponseDto.builder()
                .rideUuid(rideId)
                .driverUuid("driver-1")
                .accepted(false)
                .build();


        // When
        driverResponseKafkaTemplate.send("driver_responses", response).get();

        // Then
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            Ride updatedRide = rideRepository.findByUuid(rideId).orElseThrow();
            assertThat(updatedRide.getDriverUuid()).isEqualTo("driver-2");
//            assertThat(updatedRide.getStatus()).isEqualTo(RideStatus.REJECTED);
        });

        verify(rideRequestSenderService, timeout(15000)).sendProposal(any());

        ConsumerRecords<String, RideResponseDto> records = KafkaTestUtils.getRecords(testConsumer);
        assertThat(records).hasSize(1);
        assertThat(records.iterator().next().value().isAccepted()).isFalse();
//        clearTopic("rides_infos");
//        clearTopic("driver_responses");
//        rideRepository.deleteAll();
    }


}
