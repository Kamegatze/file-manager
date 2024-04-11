package com.kamegatze.file.manager.service;

import com.kamegatze.file.manager.models.Users;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.validation.annotation.Validated;

@Validated
public interface UsersService {

    @KafkaListener(groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topics.save.users}")
    void saveUsersFromKafka(ConsumerRecord<String, String> record);

    Users getUsersByLogin(@NotNull
                          @NotBlank
                          @NotEmpty
                          @Size(min = 5, message = "The login need more 5 sign")
                          String login);

    Users updateOrSaveUsers(Users users);
}
