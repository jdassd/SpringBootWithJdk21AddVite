package com.example.demo.repository;

import com.example.demo.entity.BlogPostTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogPostTagRepository extends JpaRepository<BlogPostTag, Long> {
    List<BlogPostTag> findByPostId(Long postId);
    void deleteByPostId(Long postId);
}
