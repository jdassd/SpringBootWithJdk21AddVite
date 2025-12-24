package com.example.demo.repository;

import com.example.demo.entity.SearchEngine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchEngineRepository extends JpaRepository<SearchEngine, Long> {
}
