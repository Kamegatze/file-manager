package com.kamegatze.file.manager.repositories.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

public interface UsersService {

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topics.save.users}")
    void saveUsersFromKafka(ConsumerRecord<String, String> record);

}
