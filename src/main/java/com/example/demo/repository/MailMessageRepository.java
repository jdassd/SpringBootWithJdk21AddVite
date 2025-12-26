package com.example.demo.repository;

import com.example.demo.entity.MailMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MailMessageRepository extends JpaRepository<MailMessage, Long> {
    List<MailMessage> findByUserId(Long userId);

    Page<MailMessage> findByUserId(Long userId, Pageable pageable);

    Optional<MailMessage> findByIdAndUserId(Long id, Long userId);
}
