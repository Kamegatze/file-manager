package com.kamegatze.authorization.service;

public interface EmailService {
    void sendMessage(String topic, String message, String to);
}
