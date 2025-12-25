package com.example.demo.controller;

import com.example.demo.entity.MailMessage;
import com.example.demo.repository.MailMessageRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/mail")
@CrossOrigin(origins = "*")
public class MailController {
    private final MailMessageRepository mailRepository;
    private final JavaMailSender mailSender;

    public MailController(MailMessageRepository mailRepository, JavaMailSender mailSender) {
        this.mailRepository = mailRepository;
        this.mailSender = mailSender;
    }

    @GetMapping
    public List<MailMessage> listMessages() {
        return mailRepository.findAll();
    }

    @PostMapping("/send")
    public MailMessage send(@Valid @RequestBody MailRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(request.fromAddress());
        message.setTo(request.toAddress());
        message.setSubject(request.subject());
        message.setText(request.body());
        mailSender.send(message);

        MailMessage stored = new MailMessage();
        stored.setId(System.currentTimeMillis());
        stored.setDirection("OUTBOUND");
        stored.setFromAddress(request.fromAddress());
        stored.setToAddress(request.toAddress());
        stored.setSubject(request.subject());
        stored.setBody(request.body());
        stored.setCreatedAt(Instant.now());
        mailRepository.save(stored);
        return stored;
    }

    @PostMapping("/receive")
    public MailMessage receive(@Valid @RequestBody MailRequest request) {
        MailMessage stored = new MailMessage();
        stored.setId(System.currentTimeMillis());
        stored.setDirection("INBOUND");
        stored.setFromAddress(request.fromAddress());
        stored.setToAddress(request.toAddress());
        stored.setSubject(request.subject());
        stored.setBody(request.body());
        stored.setCreatedAt(Instant.now());
        mailRepository.save(stored);
        return stored;
    }

    @PutMapping("/{id}")
    public MailMessage update(@PathVariable Long id, @Valid @RequestBody MailRequest request) {
        MailMessage stored = mailRepository.findById(id).orElseThrow();
        stored.setFromAddress(request.fromAddress());
        stored.setToAddress(request.toAddress());
        stored.setSubject(request.subject());
        stored.setBody(request.body());
        mailRepository.save(stored);
        return stored;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        mailRepository.deleteById(id);
    }

    public record MailRequest(
        @Email @NotBlank String fromAddress,
        @Email @NotBlank String toAddress,
        @NotBlank String subject,
        @NotBlank String body
    ) {
    }
}
