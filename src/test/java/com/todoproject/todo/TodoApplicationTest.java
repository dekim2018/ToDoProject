package com.todoproject.todo;

import com.todoproject.todo.entity.Todo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TodoApplicationTest {
    @Test
    void todoDefaultsToIncomplete() {
        Todo todo = new Todo("Spring 공부");
        assertThat(todo.getTitle()).isEqualTo("Spring 공부");
        assertThat(todo.isCompleted()).isFalse();
        assertThat(todo.getCreatedAt()).isNotNull();
    }

    @Test
    void todoCanBeUpdated() {
        Todo todo = new Todo("Spring 공부");
        todo.update("Spring Boot 공부", true);
        assertThat(todo.getTitle()).isEqualTo("Spring Boot 공부");
        assertThat(todo.isCompleted()).isTrue();
    }
}