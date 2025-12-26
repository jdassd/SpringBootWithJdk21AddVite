package com.example.demo.repository;

import com.example.demo.entity.MailOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MailOutboxRepository extends JpaRepository<MailOutbox, Long> {
    List<MailOutbox> findTop20ByStatusInOrderByCreatedAtAsc(List<String> statuses);
}
