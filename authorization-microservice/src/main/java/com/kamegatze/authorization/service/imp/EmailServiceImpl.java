package com.kamegatze.authorization.service.imp;

import com.kamegatze.authorization.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    @Override
    public void sendHtmlMessage(String to, String topic, String body) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(topic);
        helper.setText(body, true);
        emailSender.send(message);
    }
}
