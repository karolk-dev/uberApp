package com.server_app.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic rideRequests() {
        return TopicBuilder.name("ride_requests")
                .partitions(2)
                .build();
    }

    @Bean
    public NewTopic driverProposal() {
        return TopicBuilder.name("ride_proposal")
                .partitions(2)
                .build();
    }

    @Bean
    public NewTopic driverResponses() {
        return TopicBuilder.name("driver_responses")
                .partitions(2)
                .build();
    }

    @Bean
    public NewTopic ridesInfos() {
        return TopicBuilder.name("rides_infos")
                .partitions(2)
                .build();
    }

    @Bean
    public NewTopic ridesSelection() {
        return TopicBuilder.name("rides_info")
                .partitions(2)
                .build();
    }

    @Bean
    public NewTopic ridesInfoResponse() {
        return TopicBuilder.name("rides_info_response")
                .partitions(2)
                .build();
    }

    @Bean
    public NewTopic chatMessages() {
        return TopicBuilder.name("chat-messages")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic driverPosition() {
        return TopicBuilder.name("driver_position")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic chatDriverToServer() {
        return TopicBuilder.name("chat_driver_to_server")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic chatClientToServer() {
        return TopicBuilder.name("chat_client_to_server")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic chatServerToClient() {
        return TopicBuilder.name("chat_server_to_client")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic chatServerToDriver() {
        return TopicBuilder.name("chat_server_to_driver")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
