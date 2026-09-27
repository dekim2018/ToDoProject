package com.todoproject.todo.service;

import com.todoproject.common.exception.BusinessException;
import com.todoproject.common.exception.ErrorCode;
import com.todoproject.todo.dto.TodoCreateRequest;
import com.todoproject.todo.dto.TodoListResponse;
import com.todoproject.todo.dto.TodoResponse;
import com.todoproject.todo.dto.TodoUpdateRequest;
import com.todoproject.todo.entity.Todo;
import com.todoproject.todo.repository.TodoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Transactional
    public TodoResponse create(TodoCreateRequest request) {
        Todo todo = new Todo(request.title().trim());
        return TodoResponse.from(todoRepository.save(todo));
    }

    public TodoResponse findById(Long id) {
        return TodoResponse.from(findEntity(id));
    }

    public TodoListResponse findAll(int page, int size, Boolean completed) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Todo> result = completed == null
                ? todoRepository.findAll(pageable)
                : todoRepository.findByCompleted(completed, pageable);
        return new TodoListResponse(
                result.getContent().stream().map(TodoResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public TodoResponse update(Long id, TodoUpdateRequest request) {
        if (request.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        Todo todo = findEntity(id);
        if (request.title() != null && request.title().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        todo.update(request.title() == null ? null : request.title().trim(), request.completed());
        return TodoResponse.from(todo);
    }

    @Transactional
    public void delete(Long id) {
        todoRepository.delete(findEntity(id));
    }

    private Todo findEntity(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TODO_NOT_FOUND));
    }
}
