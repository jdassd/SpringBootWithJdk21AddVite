package com.example.demo.service;

import com.example.demo.entity.MailOutbox;
import com.example.demo.repository.MailOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class MailOutboxService {
    private static final Logger log = LoggerFactory.getLogger(MailOutboxService.class);

    private final MailOutboxRepository outboxRepository;
    private final JavaMailSender mailSender;

    @Value("${app.mail.max-attempts:3}")
    private int maxAttempts;

    public MailOutboxService(MailOutboxRepository outboxRepository, JavaMailSender mailSender) {
        this.outboxRepository = outboxRepository;
        this.mailSender = mailSender;
    }

    public MailOutbox enqueue(Long userId, String from, String to, String subject, String body) {
        MailOutbox outbox = new MailOutbox();
        outbox.setUserId(userId);
        outbox.setFromAddress(from);
        outbox.setToAddress(to);
        outbox.setSubject(subject);
        outbox.setBody(body);
        outbox.setStatus("QUEUED");
        outbox.setAttempts(0);
        outbox.setCreatedAt(Instant.now());
        outbox.setUpdatedAt(Instant.now());
        return outboxRepository.save(outbox);
    }

    @Scheduled(fixedDelayString = "${app.mail.outbox-interval:30000}")
    public void processOutbox() {
        List<MailOutbox> pending = outboxRepository.findTop20ByStatusInOrderByCreatedAtAsc(List.of("QUEUED", "FAILED"));
        for (MailOutbox message : pending) {
            int attempts = message.getAttempts() == null ? 0 : message.getAttempts();
            if (attempts >= maxAttempts) {
                continue;
            }
            try {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setFrom(message.getFromAddress());
                mail.setTo(message.getToAddress());
                mail.setSubject(message.getSubject());
                mail.setText(message.getBody());
                mailSender.send(mail);
                message.setStatus("SENT");
                message.setSentAt(Instant.now());
                message.setUpdatedAt(Instant.now());
                outboxRepository.save(message);
            } catch (Exception ex) {
                attempts += 1;
                message.setAttempts(attempts);
                message.setStatus("FAILED");
                message.setLastError(ex.getMessage());
                message.setUpdatedAt(Instant.now());
                outboxRepository.save(message);
                log.warn("Mail send failed for outbox {}: {}", message.getId(), ex.getMessage());
            }
        }
    }
}
