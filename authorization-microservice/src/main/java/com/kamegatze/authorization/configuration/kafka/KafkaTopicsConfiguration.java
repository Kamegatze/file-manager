package com.kamegatze.authorization.configuration.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
public class KafkaTopicsConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String addressBootstrap;
    @Value("${spring.kafka.topics.save.users}")
    private String topicSaveUsers;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configuration = Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, addressBootstrap
        );
        return new KafkaAdmin(configuration);
    }

    @Bean
    public NewTopic topicSaveUsers() {
        return new NewTopic(topicSaveUsers, 1, (short) 1);
    }
}
