package com.smarttask.smarttask_backend.service;
import com.smarttask.smarttask_backend.dto.TaskCreateRequest;
import com.smarttask.smarttask_backend.dto.TaskResponse;
import com.smarttask.smarttask_backend.dto.TaskUpdateRequest;
import com.smarttask.smarttask_backend.entity.Task;
import com.smarttask.smarttask_backend.repository.TaskRepository;
import com.smarttask.smarttask_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** WHY: Keeps controllers thin; enforces ownership rules. */
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    public List<TaskResponse> list(String username) {
        var user = userRepo.findByUsernameAndDeletedFalse(username).orElseThrow();
        return taskRepo.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                .map(t -> new TaskResponse(
                        t.getId().toString(),
                        t.getTitle(),
                        t.getDescription(),
                        t.isCompleted(),
                        t.getDueDate(),
                        t.getCreatedAt().toString()
                )).toList();
    }

    @Transactional
    public TaskResponse create(String username, TaskCreateRequest req) {
        var user = userRepo.findByUsernameAndDeletedFalse(username).orElseThrow();
        var t = Task.builder()
                .user(user)
                .title(req.title())
                .description(req.description())
                .dueDate(req.dueDate())
                .build();
        taskRepo.save(t);
        return new TaskResponse(
                t.getId().toString(), t.getTitle(), t.getDescription(), t.isCompleted(), t.getDueDate(), t.getCreatedAt().toString()
        );
    }

    @Transactional
    public TaskResponse update(String username, UUID taskId, TaskUpdateRequest req) {
        var user = userRepo.findByUsernameAndDeletedFalse(username).orElseThrow();
        var t = taskRepo.findById(taskId).orElseThrow();
        if (!t.getUser().getId().equals(user.getId())) throw new IllegalArgumentException("Forbidden");

        if (req.title() != null) t.setTitle(req.title());
        if (req.description() != null) t.setDescription(req.description());
        if (req.completed() != null) t.setCompleted(req.completed());
        if (req.dueDate() != null) t.setDueDate(req.dueDate());

        return new TaskResponse(
                t.getId().toString(), t.getTitle(), t.getDescription(), t.isCompleted(), t.getDueDate(), t.getCreatedAt().toString()
        );
    }

    @Transactional
    public void delete(String username, UUID taskId) {
        var user = userRepo.findByUsernameAndDeletedFalse(username).orElseThrow();
        var t = taskRepo.findById(taskId).orElseThrow();
        if (!t.getUser().getId().equals(user.getId())) throw new IllegalArgumentException("Forbidden");
        taskRepo.delete(t);
    }
}
