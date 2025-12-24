package com.example.demo.controller;

import com.example.demo.entity.TaskItem;
import com.example.demo.repository.TaskRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public List<TaskItem> listTasks() {
        return taskRepository.findAll();
    }

    @PostMapping
    public TaskItem create(@Valid @RequestBody TaskRequest request) {
        TaskItem task = new TaskItem();
        task.setId(System.currentTimeMillis());
        task.setTitle(request.title());
        task.setDueDate(request.dueDate());
        task.setReminderTime(request.reminderTime());
        task.setStatus("PENDING");
        task.setCreatedAt(Instant.now());
        taskRepository.save(task);
        return task;
    }

    @PutMapping("/{id}")
    public TaskItem update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        TaskItem task = taskRepository.findById(id).orElseThrow();
        task.setTitle(request.title());
        task.setDueDate(request.dueDate());
        task.setReminderTime(request.reminderTime());
        taskRepository.save(task);
        return task;
    }

    @PostMapping("/{id}/complete")
    public TaskItem complete(@PathVariable Long id) {
        TaskItem task = taskRepository.findById(id).orElseThrow();
        task.setStatus("COMPLETED");
        task.setCompletedAt(Instant.now());
        taskRepository.save(task);
        return task;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        taskRepository.deleteById(id);
    }

    @GetMapping("/summary")
    public TaskSummary summary(@RequestParam String period, @RequestParam(required = false) LocalDate date) {
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
        int completed = taskRepository.countCompletedBetween(start, end);
        int total = taskRepository.countTotalBetween(start, end);
        return new TaskSummary(period, start, end, completed, total);
    }

    public record TaskRequest(
        @NotBlank String title,
        LocalDate dueDate,
        Instant reminderTime
    ) {
    }

    public record TaskSummary(String period, LocalDate start, LocalDate end, int completed, int total) {
    }
}
