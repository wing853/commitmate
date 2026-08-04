package com.example.commitmate.todo;

import com.example.commitmate.core.errors.Exception403;
import com.example.commitmate.core.errors.ExceptionExpire;
import com.example.commitmate.core.errors.ExceptionNoInfo;
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
        todoList.forEach(Todo::updateStatus);
        return todoList;
    }

    @Transactional
    public void addTodo(TodoRequest.AddDTO addDTO) {
        GroupMember groupMember = gmr.findByGroupIdAndUserIdAndIsActiveTrue( // 수정
                addDTO.getGroupId(), addDTO.getUserId()
        ).orElseThrow(
                () -> new ExceptionNoInfo("그룹 멤버를 찾을 수 없습니다.")
        );

        Todo todo = addDTO.toEntity(groupMember);
        tr.save(todo);
    }

    @Transactional
    public void deleteTodo(Integer todoId, Integer userId) {
        Todo todo = tr.findById(todoId).orElseThrow(
                () -> new ExceptionNoInfo("미션을 찾을 수 없습니다.")
        );

        if (!todo.getGroupMember().getUser().getId().equals(userId)) {
            throw new Exception403("일정 삭제 권한이 없습니다(자신의 일정인지 확인하세요.)");
        }

        if(todo.isExpired()) {
            throw new ExceptionExpire("선택한 일정은 기간이 만료되어 삭제할 수 없습니다");
        }

        tr.deleteById(todoId);
    }

    @Transactional
    public void toggleDone(Integer id, Integer userId) {

        Todo todo = tr.findById(id).orElseThrow(
                () -> new ExceptionNoInfo("미션을 찾을 수 없습니다")
        );

        if (!todo.getGroupMember().getUser().getId().equals(userId)) {
            throw new Exception403("일정 삭제 권한이 없습니다(자신의 일정인지 확인하세요.)");
        }

        // 기한 초과 상태면 클릭 막기 (status 필드는 지연 갱신되므로 실시간 판정을 사용)
        if (todo.isExpired()) {
            throw new ExceptionExpire("선택한 일정은 기간이 지나 상태변경을 할 수 없습니다");
        }

        // COMPLETE <-> PENDING 토글
        if (todo.getStatus() == TodoStatus.FINISH) {
            todo.setStatus(TodoStatus.READY);
        } else {
            todo.setStatus(TodoStatus.FINISH);
        }
    }

    @Transactional
    public void updateTodo(Integer todoId, Integer userId, TodoRequest.UpdateTodoDTO updateTodoDTO) {
        Todo todo = tr.findById(todoId).orElseThrow(
                () -> new ExceptionNoInfo("미션을 찾을 수 없습니다")
        );

        if (!todo.getGroupMember().getUser().getId().equals(userId)) {
            throw new Exception403("수정권한이 없습니다.");
        }

        if(todo.isExpired()) {
            throw new ExceptionExpire("선택한 일정은 만료되어 수정할 수 없습니다.");
        }

        updateTodoDTO.applyTo(todo);

    }

    public Todo findTodo(Integer id) {
        Todo todo = tr.findById(id).orElseThrow(
                () -> new ExceptionNoInfo("일정을 찾을 수 없습니다.")
        );
        return todo;
    }

    public List<Todo> findMyTodos(Integer groupId, Integer userId) {
        List<Todo> todoList = tr.findByGroupMember_Group_IdAndGroupMember_User_Id(groupId,userId);

        return todoList;
    }



}
