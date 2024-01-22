package com.kamegatze.authorization.service.imp;

import com.kamegatze.authorization.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    @Value("${spring.mail.username}")
    private String from;
    @Override
    public void sendMessage(String topic, String message, String to) {
        SimpleMailMessage messageSimple = new SimpleMailMessage();
        messageSimple.setFrom(from);
        messageSimple.setTo(to);
        messageSimple.setSubject(topic);
        messageSimple.setText(message);
        emailSender.send(messageSimple);
    }
}
