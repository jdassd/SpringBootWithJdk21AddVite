package com.example.demo.repository;

import com.example.demo.entity.NavigationLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NavigationLinkRepository extends JpaRepository<NavigationLink, Long> {
    List<NavigationLink> findByUserId(Long userId);
}
