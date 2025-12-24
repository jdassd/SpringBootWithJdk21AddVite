package com.example.demo.repository;

import com.example.demo.entity.BlogTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogTagRepository extends JpaRepository<BlogTag, Long> {
    Optional<BlogTag> findByName(String name);
}
