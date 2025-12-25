package com.example.demo.repository;

import com.example.demo.entity.MailMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MailMessageRepository extends JpaRepository<MailMessage, Long> {
    List<MailMessage> findByUserId(Long userId);
}
