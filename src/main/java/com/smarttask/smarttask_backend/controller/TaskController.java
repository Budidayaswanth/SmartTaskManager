package com.smarttask.smarttask_backend.controller;
import com.smarttask.smarttask_backend.dto.TaskCreateRequest;
import com.smarttask.smarttask_backend.dto.TaskResponse;
import com.smarttask.smarttask_backend.dto.TaskUpdateRequest;
import com.smarttask.smarttask_backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** WHY: Protected Task CRUD endpoints (require Bearer token). */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService tasks;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> list(@AuthenticationPrincipal UserDetails ud) {
        return ResponseEntity.ok(tasks.list(ud.getUsername()));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@AuthenticationPrincipal UserDetails ud,
                                               @Valid @RequestBody TaskCreateRequest req) {
        return ResponseEntity.ok(tasks.create(ud.getUsername(), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(@AuthenticationPrincipal UserDetails ud,
                                               @PathVariable UUID id,
                                               @Valid @RequestBody TaskUpdateRequest req) {
        return ResponseEntity.ok(tasks.update(ud.getUsername(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails ud, @PathVariable UUID id) {
        tasks.delete(ud.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
