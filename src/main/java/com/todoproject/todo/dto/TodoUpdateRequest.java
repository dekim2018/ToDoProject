package com.todoproject.todo.dto;

import jakarta.validation.constraints.Size;

public record TodoUpdateRequest(
        @Size(max = 200, message = "제목은 200자 이하로 입력해주세요.")
        String title,
        Boolean completed
) {
    public boolean isEmpty() {
        return title == null && completed == null;
    }
}