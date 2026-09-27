package com.todoproject.todo.dto;

import java.util.List;

public record TodoListResponse(
        List<TodoResponse> todos,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}