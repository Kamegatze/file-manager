package com.kamegatze.file.manager.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamegatze.file.manager.models.Users;
import com.kamegatze.file.manager.repositories.UsersRepository;
import com.kamegatze.file.manager.service.UsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final ObjectMapper objectMapper;
    @Override
    public void saveUsersFromKafka(ConsumerRecord<String, String> record) {
        log.warn("Begin transaction by save user on offset: {}, value: {}",
                    record.offset(), record.value());
        try {
            Users users = objectMapper.readValue(record.value(), Users.class);
            usersRepository.save(users);
        } catch (JsonProcessingException e) {
            log.error("Error transaction by save user on offset: {}, value: {}",
                        record.offset(), record.value());
            throw new RuntimeException(e);
        }
        log.info("End transaction by save user on offset: {}, value: {}",
                    record.offset(), record.value());
    }
}
