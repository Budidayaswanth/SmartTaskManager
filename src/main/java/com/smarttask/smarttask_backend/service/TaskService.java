package com.smarttask.smarttask_backend.service;
import com.smarttask.smarttask_backend.dto.TaskCreateRequest;
import com.smarttask.smarttask_backend.dto.TaskResponse;
import com.smarttask.smarttask_backend.dto.TaskUpdateRequest;
import com.smarttask.smarttask_backend.entity.Task;
import com.smarttask.smarttask_backend.entity.TaskStatus;
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

    public List<TaskResponse> list(String username, TaskStatus statusFilter) {
        var user = userRepo.findByUsernameAndDeletedFalse(username).orElseThrow();
        var tasks = statusFilter == null
                ? taskRepo.findByUser_IdOrderByCreatedAtDesc(user.getId())
                : taskRepo.findByUser_IdAndStatusOrderByCreatedAtDesc(user.getId(), statusFilter);
        return tasks.stream().map(this::toResponse).toList();
    }

    @Transactional
    public TaskResponse create(String username, TaskCreateRequest req) {
        var user = userRepo.findByUsernameAndDeletedFalse(username).orElseThrow();
        var status = resolveIncomingStatus(req.status(), req.completed());
        var t = Task.builder()
                .user(user)
                .title(req.title())
                .description(req.description())
                .dueDate(req.dueDate())
                .status(status)
                .build();
        taskRepo.save(t);
        return toResponse(t);
    }

    @Transactional
    public TaskResponse update(String username, UUID taskId, TaskUpdateRequest req) {
        var user = userRepo.findByUsernameAndDeletedFalse(username).orElseThrow();
        var t = taskRepo.findById(taskId).orElseThrow();
        if (!t.getUser().getId().equals(user.getId())) throw new IllegalArgumentException("Forbidden");

        if (req.title() != null) t.setTitle(req.title());
        if (req.description() != null) t.setDescription(req.description());
        if (req.status() != null) {
            t.setStatus(req.status());
        } else if (req.completed() != null) {
            t.setCompleted(req.completed());
        }
        if (req.dueDate() != null) t.setDueDate(req.dueDate());

        return toResponse(t);
    }

    @Transactional
    public void delete(String username, UUID taskId) {
        var user = userRepo.findByUsernameAndDeletedFalse(username).orElseThrow();
        var t = taskRepo.findById(taskId).orElseThrow();
        if (!t.getUser().getId().equals(user.getId())) throw new IllegalArgumentException("Forbidden");
        taskRepo.delete(t);
    }

    private TaskResponse toResponse(Task task) {
        var legacyCompleted = task.getStatus() == TaskStatus.DONE;
        return new TaskResponse(
                task.getId().toString(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                legacyCompleted,
                task.getDueDate(),
                task.getCreatedAt().toString()
        );
    }

    private TaskStatus resolveIncomingStatus(TaskStatus status, Boolean completed) {
        if (status != null) {
            return status;
        }
        if (completed != null) {
            return completed ? TaskStatus.DONE : TaskStatus.TODO;
        }
        return TaskStatus.TODO;
    }
}
