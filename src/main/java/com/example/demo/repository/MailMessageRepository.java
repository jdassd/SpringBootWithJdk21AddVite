package com.example.demo.repository;

import com.example.demo.entity.MailMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailMessageRepository extends JpaRepository<MailMessage, Long> {
}
