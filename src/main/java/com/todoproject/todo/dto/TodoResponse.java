package com.todoproject.todo.dto;

import com.todoproject.todo.entity.Todo;

import java.time.LocalDateTime;

public record TodoResponse(
        Long id,
        String title,
        boolean completed,
        LocalDateTime createdAt
) {
    public static TodoResponse from(Todo todo) {
        return new TodoResponse(todo.getId(), todo.getTitle(), todo.isCompleted(), todo.getCreatedAt());
    }
}