//package com.driver_app;
//
//import com.driver_app.repository.DriverRepository;
//import com.driver_app.service.DatabaseContainer;
//import com.driver_app.service.RideRequestListenerService;
//import com.uber.common.dto.RideProposalDto;
//import com.uber.common.dto.RideRequestDto;
//import org.apache.kafka.clients.consumer.Consumer;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.test.mock.mockito.SpyBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.core.ProducerFactory;
//import org.springframework.kafka.support.serializer.JsonSerializer;
//import org.springframework.kafka.test.EmbeddedKafkaBroker;
//import org.springframework.kafka.test.context.EmbeddedKafka;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.TestPropertySource;
//import org.awaitility.Awaitility;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//import static org.awaitility.Awaitility.await;
//
//@SpringBootTest(properties = {
//        "spring.kafka.consumer.auto-offset-reset=earliest",
//        "spring.kafka.consumer.properties.spring.json.trusted.packages=com.example.yourpackage.dto"
//})
//@EmbeddedKafka(
//        topics = "ride_proposal",
//        partitions = 1,
//        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
//)
//@DirtiesContext
//@Testcontainers
//public class RideRequestListenerServiceIntegrationTest extends DatabaseContainer {
//    @Autowired
//    private EmbeddedKafkaBroker embeddedKafkaBroker;
//
//    @Autowired
//    private KafkaTemplate<String, RideProposalDto> kafkaTemplate;
//
//    @MockBean
//    private SimpMessagingTemplate simpMessagingTemplate;
//
//    @SpyBean
//    private RideRequestListenerService rideRequestListenerService;
//
//    @MockBean
//    private DriverRepository driverRepository;
//
//
//    @Test
//    public void testListenRideRequests() {
//
//        RideProposalDto rideProposal = new RideProposalDto();
//        rideProposal.setDriverUuid("driver-123");
//        rideProposal.setPolylineToClient("some-polyline");
//
//
//        kafkaTemplate.send("ride_proposal", rideProposal);
//
//
//        await().atMost(5, TimeUnit.SECONDS)
//                .untilAsserted(() -> verify(simpMessagingTemplate, times(1))
//                        .convertAndSend("/topic/driver/driver-123", rideProposal));
//    }
//}
