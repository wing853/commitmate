package com.example.commitmate.todo;

import com.example.commitmate.group.Group;
import com.example.commitmate.group.GroupRepository;
import com.example.commitmate.user.User;
import com.example.commitmate.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository tr;
    private final UserRepository ur;
    private final GroupRepository gr;

    public List<Todo> showTodo(Integer groupId) {
        List<Todo> todoList = tr.findByGroupId(groupId);
        return todoList;
    }

    @Transactional
    public void addTodo(TodoRequest.AddDTO addDTO) {
        User user = ur.findById(addDTO.getUserId()).orElseThrow(
                () -> new RuntimeException("유저를 찾을 수 없습니다.")
        );
        Group group = gr.findById(addDTO.getGroupId()).orElseThrow(
                () -> new RuntimeException("그룹을 찾을 수 없습니다.")
        );

        Todo todo = addDTO.toEntity(user,group);

        tr.save(todo);
    }

    @Transactional
    public void deleteTodo(Integer todoId, Integer userId) {
        Todo todo = tr.findById(todoId).orElseThrow(
                () -> new RuntimeException("미션을 찾을 수 없습니다.")
        );

        if(!todo.getUser().getId().equals(userId)) {
            throw new RuntimeException("권한 없음");
        }

        tr.deleteById(todoId);

    }
}
