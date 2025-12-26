package com.example.demo.controller;

import com.example.demo.dto.PagedResponse;
import com.example.demo.entity.TaskItem;
import com.example.demo.repository.TaskRepository;
import com.example.demo.util.PaginationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public PagedResponse<TaskItem> listTasks(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {
        Long userId = (Long) request.getAttribute("userId");
        Page<TaskItem> tasks = taskRepository.findByUserId(userId, PaginationUtils.toPageable(page, size, sort, "createdAt"));
        return new PagedResponse<>(tasks.getContent(), tasks.getNumber(), tasks.getSize(), tasks.getTotalElements());
    }

    @PostMapping
    public TaskItem create(HttpServletRequest request, @Valid @RequestBody TaskRequest requestData) {
        Long userId = (Long) request.getAttribute("userId");
        TaskItem task = new TaskItem();
        task.setUserId(userId);
        task.setTitle(requestData.title());
        task.setDueDate(requestData.dueDate());
        task.setReminderTime(requestData.reminderTime());
        task.setStatus("PENDING");
        task.setCreatedAt(Instant.now());
        taskRepository.save(task);
        return task;
    }

    @PutMapping("/{id}")
    public TaskItem update(HttpServletRequest requestContext, @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        Long userId = (Long) requestContext.getAttribute("userId");
        TaskItem task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        task.setTitle(request.title());
        task.setDueDate(request.dueDate());
        task.setReminderTime(request.reminderTime());
        taskRepository.save(task);
        return task;
    }

    @PostMapping("/{id}/complete")
    public TaskItem complete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        TaskItem task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        task.setStatus("COMPLETED");
        task.setCompletedAt(Instant.now());
        taskRepository.save(task);
        return task;
    }

    @DeleteMapping("/{id}")
    public void delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        TaskItem task = taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        taskRepository.deleteById(task.getId());
    }

    @GetMapping("/summary")
    public TaskSummary summary(HttpServletRequest request, @RequestParam String period,
            @RequestParam(required = false) LocalDate date) {
        Long userId = (Long) request.getAttribute("userId");
        LocalDate base = date == null ? LocalDate.now() : date;
        LocalDate start;
        LocalDate end;
        switch (period.toLowerCase()) {
            case "week" -> {
                start = base.with(java.time.DayOfWeek.MONDAY);
                end = base.with(java.time.DayOfWeek.SUNDAY);
            }
            case "month" -> {
                start = base.with(TemporalAdjusters.firstDayOfMonth());
                end = base.with(TemporalAdjusters.lastDayOfMonth());
            }
            case "year" -> {
                start = base.with(TemporalAdjusters.firstDayOfYear());
                end = base.with(TemporalAdjusters.lastDayOfYear());
            }
            default -> throw new IllegalArgumentException("Unsupported period");
        }
        int completed = taskRepository.countCompletedBetween(userId, start, end);
        int total = taskRepository.countTotalBetween(userId, start, end);
        return new TaskSummary(period, start, end, completed, total);
    }

    public record TaskRequest(
            @NotBlank String title,
            LocalDate dueDate,
            Instant reminderTime) {
    }

    public record TaskSummary(String period, LocalDate start, LocalDate end, int completed, int total) {
    }
}
