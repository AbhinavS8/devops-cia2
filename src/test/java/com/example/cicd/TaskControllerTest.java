
package com.example.cicd;

import com.example.cicd.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Test
    void getTasksReturnsTaskList() throws Exception {
        when(taskService.getAllTasks()).thenReturn(
            List.of(new Task(1L, "Test task", false))
        );

        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Test task"));
    }

    @Test
    void addTaskCreatesTask() throws Exception {
        when(taskService.addTask("New task"))
            .thenReturn(new Task(1L, "New task", false));

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"New task\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("New task"));
    }

    @Test
    void addTaskRejectsBlankTitle() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\" \"}"))
            .andExpect(status().isBadRequest());

        verify(taskService, never()).addTask(anyString());
    }

    @Test
    void updateTaskChangesCompletionStatus() throws Exception {
        when(taskService.updateTask(1L, true))
            .thenReturn(new Task(1L, "Test task", true));

        mockMvc.perform(patch("/api/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"completed\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void deleteTaskReturnsNoContent() throws Exception {
        when(taskService.deleteTask(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/tasks/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteMissingTaskReturnsNotFound() throws Exception {
        when(taskService.deleteTask(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/tasks/99"))
            .andExpect(status().isNotFound());
    }
}
