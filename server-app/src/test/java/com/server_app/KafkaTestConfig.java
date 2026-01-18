package com.server_app;


import com.uber.common.dto.DriverResponseDto;
import com.uber.common.dto.RideResponseDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTestConfig {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Bean
    public ProducerFactory<String, DriverResponseDto> driverResponseProducerFactory() {
        Map<String, Object> props = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, DriverResponseDto> driverResponseKafkaTemplate() {
        return new KafkaTemplate<>(driverResponseProducerFactory());
    }


    @Bean
    public ProducerFactory<String, RideResponseDto> rideResponseProducerFactory() {
        Map<String, Object> props = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, RideResponseDto> rideResponseKafkaTemplate() {
        return new KafkaTemplate<>(rideResponseProducerFactory());
    }

}
