package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.MailMessage;
import com.example.demo.repository.MailMessageRepository;
import com.example.demo.service.MailOutboxService;
import com.example.demo.util.PaginationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestController
@RequestMapping("/api/mail")
@CrossOrigin(origins = "*")
public class MailController {
    private final MailMessageRepository mailRepository;
    private final MailOutboxService outboxService;

    public MailController(MailMessageRepository mailRepository, MailOutboxService outboxService) {
        this.mailRepository = mailRepository;
        this.outboxService = outboxService;
    }

    @GetMapping
    public PagedResponse<MailMessage> listMessages(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        Long userId = (Long) request.getAttribute("userId");
        Page<MailMessage> messages = mailRepository.findByUserId(userId,
                PaginationUtils.toPageable(page, size, sort, "createdAt"));
        return new PagedResponse<>(messages.getContent(), messages.getNumber(), messages.getSize(),
                messages.getTotalElements());
    }

    @PostMapping("/send")
    public MailMessage send(HttpServletRequest request, @Valid @RequestBody MailRequest mailRequest) {
        Long userId = (Long) request.getAttribute("userId");
        outboxService.enqueue(userId, mailRequest.fromAddress(), mailRequest.toAddress(),
                mailRequest.subject(), mailRequest.body());

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
    public MailMessage update(HttpServletRequest requestContext, @PathVariable Long id,
            @Valid @RequestBody MailRequest request) {
        Long userId = (Long) requestContext.getAttribute("userId");
        MailMessage stored = mailRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mail not found"));
        stored.setFromAddress(request.fromAddress());
        stored.setToAddress(request.toAddress());
        stored.setSubject(request.subject());
        stored.setBody(request.body());
        mailRepository.save(stored);
        return stored;
    }

    @DeleteMapping("/{id}")
    public void delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        MailMessage stored = mailRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mail not found"));
        mailRepository.deleteById(stored.getId());
    }

    public record MailRequest(
            @Email @NotBlank String fromAddress,
            @Email @NotBlank String toAddress,
            @NotBlank String subject,
            @NotBlank String body) {
    }
}
