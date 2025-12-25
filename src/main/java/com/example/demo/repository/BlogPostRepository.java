package com.example.demo.repository;

import com.example.demo.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findByUserId(Long userId);

    List<BlogPost> findByStatus(String status);

    List<BlogPost> findByUserIdAndStatus(Long userId, String status);
}
