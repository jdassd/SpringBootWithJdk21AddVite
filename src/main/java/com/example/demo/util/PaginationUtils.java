package com.example.demo.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtils {
    private PaginationUtils() {
    }

    public static Pageable toPageable(int page, int size, String sort, String defaultSortField) {
        int safeSize = size <= 0 ? 20 : Math.min(size, 200);
        Sort sortSpec = Sort.by(defaultSortField).descending();
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String field = parts[0].trim();
            String direction = parts.length > 1 ? parts[1].trim() : "asc";
            sortSpec = "desc".equalsIgnoreCase(direction)
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
        }
        return PageRequest.of(Math.max(page, 0), safeSize, sortSpec);
    }
}
