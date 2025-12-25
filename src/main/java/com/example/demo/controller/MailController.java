package com.example.demo.controller;

import com.example.demo.entity.MailMessage;
import com.example.demo.repository.MailMessageRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    public List<MailMessage> listMessages(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return mailRepository.findByUserId(userId);
    }

    @PostMapping("/send")
    public MailMessage send(HttpServletRequest request, @Valid @RequestBody MailRequest mailRequest) {
        Long userId = (Long) request.getAttribute("userId");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailRequest.fromAddress());
        message.setTo(mailRequest.toAddress());
        message.setSubject(mailRequest.subject());
        message.setText(mailRequest.body());
        mailSender.send(message);

        MailMessage stored = new MailMessage();
        stored.setUserId(userId);
        stored.setDirection("OUTBOUND");
        stored.setFromAddress(mailRequest.fromAddress());
        stored.setToAddress(mailRequest.toAddress());
        stored.setSubject(mailRequest.subject());
        stored.setBody(mailRequest.body());
        stored.setCreatedAt(Instant.now());
        mailRepository.save(stored);
        return stored;
    }

    @PostMapping("/receive")
    public MailMessage receive(HttpServletRequest request, @Valid @RequestBody MailRequest mailRequest) {
        Long userId = (Long) request.getAttribute("userId");
        MailMessage stored = new MailMessage();
        stored.setUserId(userId);
        stored.setDirection("INBOUND");
        stored.setFromAddress(mailRequest.fromAddress());
        stored.setToAddress(mailRequest.toAddress());
        stored.setSubject(mailRequest.subject());
        stored.setBody(mailRequest.body());
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
            @NotBlank String body) {
    }
}
