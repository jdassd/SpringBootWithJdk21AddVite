package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.SearchEngine;
import com.example.demo.repository.SearchEngineRepository;
import com.example.demo.util.PaginationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/search-engines")
@CrossOrigin(origins = "*")
public class SearchEngineController {
    private final SearchEngineRepository repository;

    public SearchEngineController(SearchEngineRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public PagedResponse<SearchEngine> list(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        Long userId = (Long) request.getAttribute("userId");
        Page<SearchEngine> engines = repository.findByUserId(userId, PaginationUtils.toPageable(page, size, sort, "name"));
        return new PagedResponse<>(engines.getContent(), engines.getNumber(), engines.getSize(), engines.getTotalElements());
    }

    @PostMapping
    public SearchEngine create(HttpServletRequest request, @Valid @RequestBody SearchEngineRequest engineRequest) {
        Long userId = (Long) request.getAttribute("userId");
        SearchEngine engine = new SearchEngine();
        engine.setUserId(userId);
        engine.setName(engineRequest.name());
        engine.setQueryUrl(engineRequest.queryUrl());
        engine.setIsDefault(Boolean.TRUE.equals(engineRequest.isDefault()));
        if (Boolean.TRUE.equals(engineRequest.isDefault())) {
            unsetDefaults(userId);
        }
        repository.save(engine);
        return engine;
    }

    @PutMapping("/{id}")
    public SearchEngine update(HttpServletRequest request, @PathVariable Long id,
            @Valid @RequestBody SearchEngineRequest engineRequest) {
        Long userId = (Long) request.getAttribute("userId");
        SearchEngine engine = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Search engine not found"));
        engine.setName(engineRequest.name());
        engine.setQueryUrl(engineRequest.queryUrl());
        engine.setIsDefault(Boolean.TRUE.equals(engineRequest.isDefault()));
        if (Boolean.TRUE.equals(engineRequest.isDefault())) {
            unsetDefaults(userId);
        }
        repository.save(engine);
        return engine;
    }

    @DeleteMapping("/{id}")
    public void delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        SearchEngine engine = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Search engine not found"));
        repository.deleteById(engine.getId());
    }

    private void unsetDefaults(Long userId) {
        repository.findByUserId(userId).forEach(engine -> {
            if (Boolean.TRUE.equals(engine.getIsDefault())) {
                engine.setIsDefault(false);
                repository.save(engine);
            }
        });
    }

    public record SearchEngineRequest(
            @NotBlank String name,
            @NotBlank String queryUrl,
            Boolean isDefault) {
    }
}
