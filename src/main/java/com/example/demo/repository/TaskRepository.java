package com.example.demo.repository;

import com.example.demo.entity.TaskItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface TaskRepository extends JpaRepository<TaskItem, Long> {
    @Query("select count(t) from TaskItem t where t.status = 'COMPLETED' and t.dueDate between :start and :end")
    int countCompletedBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("select count(t) from TaskItem t where t.dueDate between :start and :end")
    int countTotalBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
