
package com.example.cicd;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
            "application", "DevOps CI/CD Task Tracker",
            "version", "1.0.0",
            "status", "Running"
        );
    }

    @GetMapping("/api/tasks")
    public List<Map<String, Object>> tasks() {
        return List.of(
            Map.of("id", 1, "title", "Build with Maven", "completed", true),
            Map.of("id", 2, "title", "Deploy with Kubernetes", "completed", false)
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
