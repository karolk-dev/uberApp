package com.server_app.integrationTests;

import com.server_app.model.Ride;
import com.server_app.repository.RideRepository;
import com.server_app.routing.GoogleRoutesService;
import com.server_app.routing.RouteInfo;
import com.server_app.service.DriverFinder;
import com.server_app.service.PaymentService;
import com.stripe.model.PaymentIntent;
import com.uber.common.Coordinates;
import com.uber.common.dto.DriverDto;
import com.uber.common.dto.RideRequestDto;
import com.uber.common.dto.RideResponseDto;
import com.uber.common.model.RideStatus;
import com.uber.common.productSelector.Product;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"ride_requests", "ride_proposal"})
@ActiveProfiles("test")
@EnableKafka
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ClientRideRequestServiceIntegrationTest {

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
    }

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private KafkaTemplate<String, RideRequestDto> rideRequestDtoKafkaTemplate;

    @Autowired
    private RideRepository rideRepository;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private GoogleRoutesService googleRoutesService;

    @MockBean
    private DriverFinder finder;

    @BeforeEach
    void setup() {
        rideRepository.deleteAll();
    }

    private void clearTopic(String topicName) {
        Consumer<String, RideResponseDto> cleaner = createConsumer(topicName);
        ConsumerRecords<String, RideResponseDto> records;
        do {
            records = KafkaTestUtils.getRecords(cleaner, Duration.ofMillis(500));
        } while (!records.isEmpty());
        cleaner.close();
    }
    private Consumer<String, RideResponseDto> createConsumer(String topicName) {
        Map<String, Object> props = KafkaTestUtils.consumerProps("cleaner-group", "true", embeddedKafka);
        props.put("key.deserializer", StringDeserializer.class);
        props.put("value.deserializer", JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        DefaultKafkaConsumerFactory<String, RideResponseDto> cf = new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(RideResponseDto.class)
        );
        Consumer<String, RideResponseDto> consumer = cf.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, topicName);
        return consumer;
    }

    @Test
    void shouldConsumeRideRequestsTopicAndSaveRide() throws Exception {
        DriverDto dummyDriver = new DriverDto();
        dummyDriver.setUuid("driver-uuid-999");
        when(finder.findNearbyDriver(any(), anyInt(), any()))
                .thenReturn(dummyDriver);

        RouteInfo routeInfo = RouteInfo.builder()
                .distanceInMeters(4000)
                .etaInMinutes(30)
                .polyline("encoded_polyline_123")
                .build();
        when(googleRoutesService.getRouteInfo(any(), any()))
                .thenReturn(routeInfo);

        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setId("pi_test123");
        when(paymentService.createPaymentIntent(anyString(), anyLong(), anyString(), anyString()))
                .thenReturn(paymentIntent);

        RideRequestDto requestDto = RideRequestDto.builder()
                .clientUuid("test-client-uuid")
                .customerId("test-customer-uuid")
                .pickupLocation(new Coordinates(50.00, 51.00))
                .destinationLocation(new Coordinates(50.00, 51.10))
                .status("new")
                .paymentMethodId("test-paymentId")
                .product(Product.UberX)
                .build();


        rideRequestDtoKafkaTemplate.send("ride_requests", requestDto);

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var rides = rideRepository.findAll();
                    assertEquals(1, rides.size());

                    Ride ride = rides.get(0);
                    assertEquals("test-client-uuid", ride.getClientUuid());
                    assertEquals("driver-uuid-999", ride.getDriverUuid());
                    assertEquals("pi_test123", ride.getPaymentIntendId());
                    assertEquals("encoded_polyline_123", ride.getPolyline());
                    assertNotNull(ride.getCreatedAt());
                    assertNotNull(ride.getSearchStartTime());
                    assertEquals(RideStatus.PENDING, ride.getStatus());
                });

//        clearTopic("rides_infos");
//        clearTopic("driver_responses");
//        rideRepository.deleteAll();

    }

}
