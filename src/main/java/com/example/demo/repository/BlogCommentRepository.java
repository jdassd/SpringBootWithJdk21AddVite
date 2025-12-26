package com.example.demo.repository;

import com.example.demo.entity.BlogComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {
    List<BlogComment> findByPostId(Long postId);

    List<BlogComment> findByPostIdAndStatus(Long postId, String status);

    @Query("""
        select c from BlogComment c
        join BlogPost p on c.postId = p.id
        where p.userId = :userId
          and (:status is null or c.status = :status)
        """)
    Page<BlogComment> findByOwnerAndStatus(@Param("userId") Long userId, @Param("status") String status,
            Pageable pageable);
}
