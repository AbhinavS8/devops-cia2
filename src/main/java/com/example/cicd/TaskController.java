
package com.example.cicd;

import com.example.cicd.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getTasks() {
        return taskService.getAllTasks();
    }

    @PostMapping
    public ResponseEntity<?> addTask(
        @RequestBody Map<String, String> request
    ) {
        String title = request.get("title");

        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Task title is required"));
        }

        Task task = taskService.addTask(title.trim());

        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTask(
        @PathVariable Long id,
        @RequestBody Map<String, Boolean> request
    ) {
        Boolean completed = request.get("completed");

        if (completed == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Completed status is required"));
        }

        Task updated = taskService.updateTask(id, completed);

        if (updated == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (!taskService.deleteTask(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
