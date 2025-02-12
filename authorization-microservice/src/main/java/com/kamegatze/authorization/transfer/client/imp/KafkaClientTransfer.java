package com.kamegatze.authorization.transfer.client.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kamegatze.authorization.transfer.client.ClientTransfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaClientTransfer implements ClientTransfer<Object> {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void sendData(Object body, String topic) {
        try {
            String json = objectMapper.writeValueAsString(body);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, json);
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.warn("Message was not sent. Error: {}", throwable.getMessage());
                    throw new RuntimeException(throwable);
                }
                log.info("Sent message: {}, with offset: {}", body, result.getRecordMetadata().offset());
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
