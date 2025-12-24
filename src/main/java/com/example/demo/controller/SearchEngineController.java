package com.example.demo.controller;

import com.example.demo.entity.SearchEngine;
import com.example.demo.repository.SearchEngineRepository;
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
    public List<SearchEngine> list() {
        return repository.findAll();
    }

    @GetMapping("/public")
    public List<SearchEngine> listPublic() {
        return list();
    }

    @PostMapping
    public SearchEngine create(@Valid @RequestBody SearchEngineRequest request) {
        SearchEngine engine = new SearchEngine();
        engine.setId(System.currentTimeMillis());
        engine.setName(request.name());
        engine.setQueryUrl(request.queryUrl());
        engine.setIsDefault(Boolean.TRUE.equals(request.isDefault()));
        if (Boolean.TRUE.equals(request.isDefault())) {
            unsetDefaults();
        }
        repository.save(engine);
        return engine;
    }

    @PutMapping("/{id}")
    public SearchEngine update(@PathVariable Long id, @Valid @RequestBody SearchEngineRequest request) {
        SearchEngine engine = repository.findById(id).orElseThrow();
        engine.setName(request.name());
        engine.setQueryUrl(request.queryUrl());
        engine.setIsDefault(Boolean.TRUE.equals(request.isDefault()));
        if (Boolean.TRUE.equals(request.isDefault())) {
            unsetDefaults();
        }
        repository.save(engine);
        return engine;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private void unsetDefaults() {
        repository.findAll().forEach(engine -> {
            if (Boolean.TRUE.equals(engine.getIsDefault())) {
                engine.setIsDefault(false);
                repository.save(engine);
            }
        });
    }

    public record SearchEngineRequest(
        @NotBlank String name,
        @NotBlank String queryUrl,
        Boolean isDefault
    ) {
    }
}
