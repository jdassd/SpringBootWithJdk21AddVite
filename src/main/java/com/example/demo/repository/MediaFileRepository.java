package com.example.demo.repository;

import com.example.demo.entity.MediaFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    Page<MediaFile> findByUserId(Long userId, Pageable pageable);
    Optional<MediaFile> findByIdAndUserId(Long id, Long userId);
}
