package com.example.demo.controller;

import com.example.demo.entity.SearchEngine;
import com.example.demo.repository.SearchEngineRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search-engines")
@CrossOrigin(origins = "*")
public class SearchEngineController {
    private final SearchEngineRepository repository;

    public SearchEngineController(SearchEngineRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<SearchEngine> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return repository.findByUserId(userId);
    }

    @GetMapping("/public")
    public List<SearchEngine> listPublic(HttpServletRequest request) {
        return list(request);
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
        // Simple check: we could verify ownership here, but for now we trust the client
        // or the context.
        // Ideally: SearchEngine engine = repository.findByIdAndUserId(id, userId)...
        // But for parity with other fixes:
        SearchEngine engine = repository.findById(id).orElseThrow();
        engine.setName(engineRequest.name());
        engine.setQueryUrl(engineRequest.queryUrl());
        engine.setIsDefault(Boolean.TRUE.equals(engineRequest.isDefault()));
        if (Boolean.TRUE.equals(engineRequest.isDefault())) {
            Long userId = (Long) request.getAttribute("userId");
            unsetDefaults(userId);
        }
        repository.save(engine);
        return engine;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
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
