package com.kamegatze.authorization.services;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendMessage(String topic, String message, String to);
    void sendHtmlMessage(String to, String topic, String body) throws MessagingException;
}
