package com.pablovass.authservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuración de Kafka para la creación de topics y productores.
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.user-events}")
    private String userEventsTopic;

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(userEventsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
