package com.example.commitmate.todo;

import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.groupmember.GroupMemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository tr;
    private final GroupMemberRepository gmr;

    public List<Todo> showTodo(Integer groupId) {
        List<Todo> todoList = tr.findByGroupIdWithMember(groupId);
        return todoList;
    }

    @Transactional
    public void addTodo(TodoRequest.AddDTO addDTO) {
        GroupMember groupMember = gmr.findByGroupIdAndUserId(
                addDTO.getGroupId(), addDTO.getUserId()
        ).orElseThrow(
                () -> new RuntimeException("그룹 멤버를 찾을 수 없습니다.")
        );

        Todo todo = addDTO.toEntity(groupMember);

        tr.save(todo);
    }

    @Transactional
    public void deleteTodo(Integer todoId, Integer userId) {
        Todo todo = tr.findById(todoId).orElseThrow(
                () -> new RuntimeException("미션을 찾을 수 없습니다.")
        );

        if (!todo.getGroupMember().getUser().getId().equals(userId)) {
            throw new RuntimeException("권한 없음");
        }

        tr.deleteById(todoId);
    }

    @Transactional
    public void toggleDone(Integer id, Integer userId) {

        Todo todo = tr.findById(id).orElseThrow(
                () -> new RuntimeException("미션을 찾을 수 없습니다")
        );

        if (!todo.getGroupMember().getUser().getId().equals(userId)) {
            throw new RuntimeException("자신의 일정이 아닙니다");
        }

        // 기한 초과 상태면 클릭 막기
        if (todo.getStatus() == TodoStatus.EXPIRED) {
            throw new RuntimeException("기한이 지난 미션입니다");
        }

        // COMPLETE <-> PENDING 토글
        if (todo.getStatus() == TodoStatus.FINISH) {
            todo.setStatus(TodoStatus.READY);
        } else {
            todo.setStatus(TodoStatus.FINISH);
        }
    }

    public List<Todo> findMyTodos(Integer groupId, Integer userId) {
        List<Todo> todoList = tr.findByGroupMember_Group_IdAndGroupMember_User_Id(groupId,userId);

        return todoList;
    }
}
