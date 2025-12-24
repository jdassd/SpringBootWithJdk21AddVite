package com.example.demo.repository;

import com.example.demo.entity.CustomPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomPageRepository extends JpaRepository<CustomPage, Long> {
    Optional<CustomPage> findBySlug(String slug);
}
