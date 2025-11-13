package com.smarttask.smarttask_backend.repository;

import com.smarttask.smarttask_backend.entity.Task;
import com.smarttask.smarttask_backend.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByUser_IdOrderByCreatedAtDesc(UUID userId);
    List<Task> findByUser_IdAndStatusOrderByCreatedAtDesc(UUID userId, TaskStatus status);
}
