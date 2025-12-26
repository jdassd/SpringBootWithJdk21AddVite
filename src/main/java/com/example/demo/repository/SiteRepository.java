package com.example.demo.repository;

import com.example.demo.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Long> {
    Optional<Site> findBySiteKey(String siteKey);
    Optional<Site> findByOwnerUserId(Long ownerUserId);
    boolean existsBySiteKey(String siteKey);
}
