package com.example.demo.repository;

import com.example.demo.entity.CustomPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomPageRepository extends JpaRepository<CustomPage, Long> {
    List<CustomPage> findByUserId(Long userId);

    Page<CustomPage> findByUserId(Long userId, Pageable pageable);

    Page<CustomPage> findByUserIdAndVisibility(Long userId, String visibility, Pageable pageable);

    Optional<CustomPage> findBySlugAndUserId(String slug, Long userId);

    Optional<CustomPage> findByIdAndUserId(Long id, Long userId);
}
