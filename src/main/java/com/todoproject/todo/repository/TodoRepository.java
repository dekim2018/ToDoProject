package com.todoproject.todo.repository;

import com.todoproject.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    Page<Todo> findByCompleted(Boolean completed, Pageable pageable);
}
