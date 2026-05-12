package com.example.commitmate.todo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository tr;

    public List<Todo> showTodo() {
        List<Todo> todoList = tr.findAll();
        return todoList;
    }
}
