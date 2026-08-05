package com.example.commitmate.todo;

import com.example.commitmate.core.errors.Exception403;
import com.example.commitmate.core.errors.ExceptionExpire;
import com.example.commitmate.core.errors.ExceptionNoInfo;
import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.groupmember.GroupMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository tr;
    @Mock
    private GroupMemberRepository gmr;

    @InjectMocks
    private TodoService todoService;

    private GroupMember memberOf(Integer userId) {
        return memberOf(userId, 1);
    }

    private GroupMember memberOf(Integer userId, Integer groupMemberId) {
        com.example.commitmate.user.User user = new com.example.commitmate.user.User();
        user.setId(userId);
        GroupMember gm = GroupMember.builder().user(user).build();
        gm.setId(groupMemberId);
        return gm;
    }

    @Test
    void toggleDone_READY에서_FINISH로_전환된다() {
        Todo todo = Todo.builder().status(TodoStatus.READY).groupMember(memberOf(10)).build();
        todo.setId(1);
        when(tr.findById(1)).thenReturn(Optional.of(todo));

        todoService.toggleDone(1, 10);

        assertThat(todo.getStatus()).isEqualTo(TodoStatus.FINISH);
    }

    @Test
    void toggleDone_FINISH에서_READY로_전환된다() {
        Todo todo = Todo.builder().status(TodoStatus.FINISH).groupMember(memberOf(10)).build();
        todo.setId(1);
        when(tr.findById(1)).thenReturn(Optional.of(todo));

        todoService.toggleDone(1, 10);

        assertThat(todo.getStatus()).isEqualTo(TodoStatus.READY);
    }

    @Test
    void toggleDone_만료된_일정은_예외() {
        Todo todo = Todo.builder().status(TodoStatus.EXPIRED).groupMember(memberOf(10)).build();
        todo.setId(1);
        when(tr.findById(1)).thenReturn(Optional.of(todo));

        assertThatThrownBy(() -> todoService.toggleDone(1, 10))
                .isInstanceOf(ExceptionExpire.class);
    }

    @Test
    void toggleDone_본인_일정이_아니면_예외() {
        Todo todo = Todo.builder().status(TodoStatus.READY).groupMember(memberOf(10)).build();
        todo.setId(1);
        when(tr.findById(1)).thenReturn(Optional.of(todo));

        assertThatThrownBy(() -> todoService.toggleDone(1, 999))
                .isInstanceOf(Exception403.class);
    }

    @Test
    void deleteTodo_만료된_일정은_삭제할_수_없다() {
        Todo todo = Todo.builder().status(TodoStatus.EXPIRED).groupMember(memberOf(10)).build();
        todo.setId(1);
        when(tr.findById(1)).thenReturn(Optional.of(todo));

        assertThatThrownBy(() -> todoService.deleteTodo(1, 10))
                .isInstanceOf(ExceptionExpire.class);
    }

    @Test
    void deleteTodo_정상삭제() {
        Todo todo = Todo.builder().status(TodoStatus.READY).groupMember(memberOf(10)).build();
        todo.setId(1);
        when(tr.findById(1)).thenReturn(Optional.of(todo));

        todoService.deleteTodo(1, 10);

        verify(tr).deleteById(1);
    }

    @Test
    void addTodo_그룹멤버가_없으면_예외() {
        TodoRequest.AddDTO dto = new TodoRequest.AddDTO();
        dto.setGroupId(1);
        dto.setUserId(10);
        dto.setWork("운동하기");
        when(gmr.findByGroupIdAndUserIdAndIsActiveTrue(1, 10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> todoService.addTodo(dto))
                .isInstanceOf(ExceptionNoInfo.class);
    }

    @Test
    void addTodo_정상등록() {
        TodoRequest.AddDTO dto = new TodoRequest.AddDTO();
        dto.setGroupId(1);
        dto.setUserId(10);
        dto.setWork("운동하기");
        dto.setAmount(1000);
        GroupMember gm = memberOf(10);
        when(gmr.findByGroupIdAndUserIdAndIsActiveTrue(1, 10)).thenReturn(Optional.of(gm));

        todoService.addTodo(dto);

        verify(tr).save(any(Todo.class));
    }

    @Test
    void addTodo_단체일정_정상등록() {
        TodoRequest.AddDTO dto = new TodoRequest.AddDTO();
        dto.setGroupId(1);
        dto.setUserId(10);
        dto.setWork("회식");
        dto.setAmount(2000);
        dto.setMemberIds(List.of(1, 2, 3));

        GroupMember m1 = memberOf(10, 1);
        GroupMember m2 = memberOf(11, 2);
        GroupMember m3 = memberOf(12, 3);
        when(gmr.findByGroupIdAndIsActiveTrue(1)).thenReturn(List.of(m1, m2, m3));

        todoService.addTodo(dto);

        org.mockito.ArgumentCaptor<Todo> captor = org.mockito.ArgumentCaptor.forClass(Todo.class);
        verify(tr, times(3)).save(captor.capture());

        List<Todo> saved = captor.getAllValues();
        assertThat(saved).hasSize(3);
        assertThat(saved).allSatisfy(t -> assertThat(t.getBatchId()).isNotBlank());
        assertThat(saved.stream().map(Todo::getBatchId).distinct().count()).isEqualTo(1);
    }

    @Test
    void addTodo_단체일정_그룹에_속하지않은_멤버포함시_예외() {
        TodoRequest.AddDTO dto = new TodoRequest.AddDTO();
        dto.setGroupId(1);
        dto.setUserId(10);
        dto.setWork("회식");
        dto.setAmount(2000);
        dto.setMemberIds(List.of(1, 999));

        GroupMember m1 = memberOf(10, 1);
        when(gmr.findByGroupIdAndIsActiveTrue(1)).thenReturn(List.of(m1));

        assertThatThrownBy(() -> todoService.addTodo(dto))
                .isInstanceOf(ExceptionNoInfo.class);

        verify(tr, never()).save(any());
    }
}