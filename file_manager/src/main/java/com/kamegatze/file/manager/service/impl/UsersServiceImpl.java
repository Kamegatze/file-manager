package com.kamegatze.file.manager.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamegatze.file.manager.models.FileSystem;
import com.kamegatze.file.manager.models.Users;
import com.kamegatze.file.manager.repositories.FileSystemRepository;
import com.kamegatze.file.manager.repositories.UsersRepository;
import com.kamegatze.file.manager.service.UsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final ObjectMapper objectMapper;
    private final FileSystemRepository fileSystemRepository;
    @Override
    public void saveUsersFromKafka(ConsumerRecord<String, String> record) {
        log.warn("Begin transaction by save user on offset: {}, value: {}",
                    record.offset(), record.value());
        try {
            Users users = objectMapper.readValue(record.value(), Users.class);
            users = usersRepository.save(users);
            fileSystemRepository.save(
                FileSystem.builder()
                    .user(users)
                    .isFile(Boolean.FALSE)
                    .name("root")
                    .build()
            );
        } catch (JsonProcessingException e) {
            log.error("Error transaction by save user on offset: {}, value: {}",
                        record.offset(), record.value());
            throw new RuntimeException(e);
        }
        log.info("End transaction by save user on offset: {}, value: {}",
                    record.offset(), record.value());
    }

    @Override
    public Users getUsersByLogin(String login) {
        Example<Users> request = Example.of(
                Users.builder()
                .login(login)
                .build()
        );
        return usersRepository.findOne(request)
                .orElseThrow(
                        () -> new NoSuchElementException(
                            String.format("User by login: %s not found", login)
                        )
                );
    }

    @Override
    public Users updateOrSaveUsers(Users users) {
        return usersRepository.save(users);
    }

}
