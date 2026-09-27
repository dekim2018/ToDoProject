package com.todoproject.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoCreateRequest(
        @NotBlank(message = "제목은 비어 있을 수 없습니다.")
        @Size(max = 200, message = "제목은 200자 이하로 입력해주세요.")
        String title
) {
}