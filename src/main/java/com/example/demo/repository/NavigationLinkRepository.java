package com.example.demo.repository;

import com.example.demo.entity.NavigationLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NavigationLinkRepository extends JpaRepository<NavigationLink, Long> {
    List<NavigationLink> findByUserId(Long userId);

    Page<NavigationLink> findByUserId(Long userId, Pageable pageable);

    Optional<NavigationLink> findByIdAndUserId(Long id, Long userId);
}
