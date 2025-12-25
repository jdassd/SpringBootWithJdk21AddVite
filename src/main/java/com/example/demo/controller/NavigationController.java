package com.example.demo.controller;

import com.example.demo.entity.NavigationLink;
import com.example.demo.repository.NavigationLinkRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/navigation")
@CrossOrigin(origins = "*")
public class NavigationController {
    private final NavigationLinkRepository linkRepository;

    public NavigationController(NavigationLinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @GetMapping
    public List<NavigationLink> listLinks(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return linkRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(NavigationLink::getGroupName, Comparator.nullsLast(String::compareTo))
                        .thenComparing(link -> link.getSortOrder() == null ? 0 : link.getSortOrder()))
                .toList();
    }

    @GetMapping("/public")
    public List<NavigationLink> listPublicLinks(HttpServletRequest request) {
        return listLinks(request);
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
    public NavigationLink update(@PathVariable Long id, @Valid @RequestBody NavigationRequest request) {
        NavigationLink link = linkRepository.findById(id).orElseThrow();
        link.setName(request.name());
        link.setUrl(request.url());
        link.setIcon(request.icon());
        link.setGroupName(request.groupName());
        link.setSortOrder(request.sortOrder());
        linkRepository.save(link);
        return link;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        linkRepository.deleteById(id);
    }

    public record NavigationRequest(
            @NotBlank String name,
            @NotBlank String url,
            String icon,
            String groupName,
            Integer sortOrder) {
    }
}
