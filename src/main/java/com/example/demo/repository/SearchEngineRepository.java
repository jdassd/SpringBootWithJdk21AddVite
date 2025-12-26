package com.example.demo.repository;

import com.example.demo.entity.SearchEngine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SearchEngineRepository extends JpaRepository<SearchEngine, Long> {
    List<SearchEngine> findByUserId(Long userId);

    Page<SearchEngine> findByUserId(Long userId, Pageable pageable);

    Optional<SearchEngine> findByIdAndUserId(Long id, Long userId);
}
