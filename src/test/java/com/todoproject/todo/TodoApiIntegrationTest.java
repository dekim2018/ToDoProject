package com.todoproject.todo;

import com.todoproject.todo.entity.Todo;
import com.todoproject.todo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TodoApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired TodoRepository todoRepository;

    @BeforeEach
    void setUp() { todoRepository.deleteAll(); }

    @Test
    void createTodo() throws Exception {
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Spring Boot 공부"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/todos/")))
                .andExpect(jsonPath("$.title").value("Spring Boot 공부"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void getTodo() throws Exception {
        Todo todo = todoRepository.save(new Todo("조회 테스트"));
        mockMvc.perform(get("/api/todos/{id}", todo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todo.getId()))
                .andExpect(jsonPath("$.title").value("조회 테스트"));
    }

    @Test
    void listTodos() throws Exception {
        todoRepository.save(new Todo("첫 번째"));
        Todo second = todoRepository.save(new Todo("두 번째"));
        second.update(null, true);
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todos.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
        mockMvc.perform(get("/api/todos").param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.todos.length()").value(1))
                .andExpect(jsonPath("$.todos[0].completed").value(true));
    }

    @Test
    void updateTodo() throws Exception {
        Todo todo = todoRepository.save(new Todo("수정 전"));
        mockMvc.perform(patch("/api/todos/{id}", todo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"수정 후","completed":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정 후"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void deleteTodo() throws Exception {
        Todo todo = todoRepository.save(new Todo("삭제 테스트"));
        mockMvc.perform(delete("/api/todos/{id}", todo.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/todos/{id}", todo.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void blankTitleReturns400() throws Exception {
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void overlongTitleReturns400() throws Exception {
        String title = "a".repeat(201);
        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"%s\"}".formatted(title)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void emptyUpdateReturns400() throws Exception {
        Todo todo = todoRepository.save(new Todo("수정 테스트"));
        mockMvc.perform(patch("/api/todos/{id}", todo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void notFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/todos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("TODO_NOT_FOUND"));
    }
}