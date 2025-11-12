package com.smarttask.smarttask_backend.controller;

import com.smarttask.smarttask_backend.dto.TaskCreateRequest;
import com.smarttask.smarttask_backend.dto.TaskUpdateRequest;
import com.smarttask.smarttask_backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService tasks;

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal UserDetails ud) {
        if (ud == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized - Missing or invalid token"));
        }
        return ResponseEntity.ok(tasks.list(ud.getUsername()));
    }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails ud,
                                    @Valid @RequestBody TaskCreateRequest req) {
        if (ud == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized - Missing or invalid token"));
        }
        return ResponseEntity.ok(tasks.create(ud.getUsername(), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@AuthenticationPrincipal UserDetails ud,
                                    @PathVariable UUID id,
                                    @Valid @RequestBody TaskUpdateRequest req) {
        if (ud == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized - Missing or invalid token"));
        }
        return ResponseEntity.ok(tasks.update(ud.getUsername(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails ud, @PathVariable UUID id) {
        if (ud == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized - Missing or invalid token"));
        }
        tasks.delete(ud.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
