package com.example.demo.repository;

import com.example.demo.entity.SearchEngine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchEngineRepository extends JpaRepository<SearchEngine, Long> {
    List<SearchEngine> findByUserId(Long userId);
}
