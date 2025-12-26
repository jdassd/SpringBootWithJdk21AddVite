package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.NavigationLink;
import com.example.demo.repository.NavigationLinkRepository;
import com.example.demo.util.PaginationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/navigation")
@CrossOrigin(origins = "*")
public class NavigationController {
    private final NavigationLinkRepository linkRepository;

    public NavigationController(NavigationLinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @GetMapping
    public PagedResponse<NavigationLink> listLinks(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        Long userId = (Long) request.getAttribute("userId");
        Page<NavigationLink> links = linkRepository.findByUserId(userId, PaginationUtils.toPageable(page, size, sort, "sortOrder"));
        return new PagedResponse<>(links.getContent(), links.getNumber(), links.getSize(), links.getTotalElements());
    }

    @PostMapping
    public NavigationLink create(HttpServletRequest request, @Valid @RequestBody NavigationRequest navigationRequest) {
        Long userId = (Long) request.getAttribute("userId");
        NavigationLink link = new NavigationLink();
        link.setUserId(userId);
        link.setName(navigationRequest.name());
        link.setUrl(navigationRequest.url());
        link.setIcon(navigationRequest.icon());
        link.setGroupName(navigationRequest.groupName());
        link.setSortOrder(navigationRequest.sortOrder());
        linkRepository.save(link);
        return link;
    }

    @PutMapping("/{id}")
    public NavigationLink update(HttpServletRequest request, @PathVariable Long id,
            @Valid @RequestBody NavigationRequest update) {
        Long userId = (Long) request.getAttribute("userId");
        NavigationLink link = linkRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Navigation link not found"));
        link.setName(update.name());
        link.setUrl(update.url());
        link.setIcon(update.icon());
        link.setGroupName(update.groupName());
        link.setSortOrder(update.sortOrder());
        linkRepository.save(link);
        return link;
    }

    @DeleteMapping("/{id}")
    public void delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        NavigationLink link = linkRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Navigation link not found"));
        linkRepository.deleteById(link.getId());
    }

    public record NavigationRequest(
            @NotBlank String name,
            @NotBlank String url,
            String icon,
            String groupName,
            Integer sortOrder) {
    }
}
