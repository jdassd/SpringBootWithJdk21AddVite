package com.example.demo.repository;

import com.example.demo.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findByUserId(Long userId);

    Page<BlogPost> findByUserId(Long userId, Pageable pageable);

    Page<BlogPost> findByUserIdAndVisibility(Long userId, String visibility, Pageable pageable);

    List<BlogPost> findByUserIdAndVisibility(Long userId, String visibility);

    Page<BlogPost> findByUserIdAndVisibilityAndPublishedAtBefore(Long userId, String visibility, Instant publishedAt,
            Pageable pageable);

    Optional<BlogPost> findByIdAndUserId(Long id, Long userId);

    Optional<BlogPost> findBySlugAndUserId(String slug, Long userId);
}
