
package com.example.cicd.service;

import com.example.cicd.Task;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {

    private final ConcurrentHashMap<Long, Task> tasks =
        new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(0);

    public TaskService() {
        addTask("Build application using Maven");
        addTask("Create Docker image");
        addTask("Deploy application using Kubernetes");
    }

    public List<Task> getAllTasks() {
        return tasks.values()
            .stream()
            .sorted((a, b) -> a.id().compareTo(b.id()))
            .toList();
    }

    public Task addTask(String title) {
        Long id = idGenerator.incrementAndGet();

        Task task = new Task(id, title, false);
        tasks.put(id, task);

        return task;
    }

    public Task updateTask(Long id, boolean completed) {
        return tasks.computeIfPresent(id,
            (key, existing) ->
                new Task(key, existing.title(), completed)
        );
    }

    public boolean deleteTask(Long id) {
        return tasks.remove(id) != null;
    }
}
