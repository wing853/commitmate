package com.example.commitmate.group;

import com.example.commitmate.core.errors.Exception400;
import com.example.commitmate.core.errors.Exception403;
import com.example.commitmate.core.errors.ExceptionFine;
import com.example.commitmate.core.errors.ExceptionNoInfo;
import com.example.commitmate.fine.Fine;
import com.example.commitmate.fine.FineRepository;
import com.example.commitmate.fine.FineStatus;
import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.groupmember.GroupMemberRepository;
import com.example.commitmate.groupmember.GroupRole;
import com.example.commitmate.todo.Todo;
import com.example.commitmate.todo.TodoRepository;
import com.example.commitmate.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository gr;
    @Mock
    private GroupMemberRepository gmr;
    @Mock
    private TodoRepository tr;
    @Mock
    private FineRepository fr;

    @InjectMocks
    private GroupService groupService;

    private GroupMember member(Integer id, GroupRole role) {
        GroupMember gm = GroupMember.builder().role(role).build();
        gm.setId(id);
        return gm;
    }

    @Test
    void kickMember_관리자가_아니면_예외() {
        GroupMember requester = member(1, GroupRole.MEMBER);
        when(gmr.findByGroupIdAndUserIdAndIsActiveTrue(1, 100)).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> groupService.kickMember(1, 2, 100))
                .isInstanceOf(Exception403.class);
    }

    @Test
    void kickMember_대상이_관리자면_예외() {
        GroupMember requester = member(1, GroupRole.ADMIN);
        GroupMember target = member(2, GroupRole.ADMIN);
        when(gmr.findByGroupIdAndUserIdAndIsActiveTrue(1, 100)).thenReturn(Optional.of(requester));
        when(gmr.findById(2)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> groupService.kickMember(1, 2, 100))
                .isInstanceOf(Exception403.class);
    }

    @Test
    void kickMember_미납벌금이_있으면_예외() {
        GroupMember requester = member(1, GroupRole.ADMIN);
        GroupMember target = member(2, GroupRole.MEMBER);
        when(gmr.findByGroupIdAndUserIdAndIsActiveTrue(1, 100)).thenReturn(Optional.of(requester));
        when(gmr.findById(2)).thenReturn(Optional.of(target));

        Todo expiredTodo = Todo.builder().status(com.example.commitmate.todo.TodoStatus.EXPIRED).build();
        Fine unpaidFine = Fine.builder().groupMember(target).status(FineStatus.UNPAID).build();
        unpaidFine.setTodo(expiredTodo);
        when(fr.findByGroupId(1)).thenReturn(List.of(unpaidFine));

        assertThatThrownBy(() -> groupService.kickMember(1, 2, 100))
                .isInstanceOf(ExceptionNoInfo.class);
    }

    @Test
    void kickMember_정상강퇴시_비활성화된다() {
        GroupMember requester = member(1, GroupRole.ADMIN);
        GroupMember target = member(2, GroupRole.MEMBER);
        when(gmr.findByGroupIdAndUserIdAndIsActiveTrue(1, 100)).thenReturn(Optional.of(requester));
        when(gmr.findById(2)).thenReturn(Optional.of(target));
        when(fr.findByGroupId(1)).thenReturn(List.of());
        when(tr.findByGroupMemberId(2)).thenReturn(List.of());

        groupService.kickMember(1, 2, 100);

        assertThat(target.isActive()).isFalse();
        verify(tr).deleteByGroupMemberId(2);
    }

    @Test
    void delegateAdmin_총무가_아니면_예외() {
        GroupMember requester = member(1, GroupRole.MEMBER);
        when(gmr.findByGroupIdAndUserIdAndIsActiveTrue(1, 100)).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> groupService.delegateAdmin(1, 2, 100))
                .isInstanceOf(Exception403.class);
    }

    @Test
    void delegateAdmin_정상위임시_역할이_바뀐다() {
        GroupMember requester = member(1, GroupRole.ADMIN);
        GroupMember target = member(2, GroupRole.MEMBER);
        when(gmr.findByGroupIdAndUserIdAndIsActiveTrue(1, 100)).thenReturn(Optional.of(requester));
        when(gmr.findById(2)).thenReturn(Optional.of(target));

        groupService.delegateAdmin(1, 2, 100);

        assertThat(target.getRole()).isEqualTo(GroupRole.ADMIN);
        assertThat(requester.getRole()).isEqualTo(GroupRole.MEMBER);
    }

    @Test
    void leaveGroup_총무는_나갈_수_없다() {
        GroupMember admin = member(1, GroupRole.ADMIN);
        when(gmr.findByGroupIdAndUserIdAndIsActiveTrue(1, 100)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> groupService.leaveGroup(1, 100))
                .isInstanceOf(Exception400.class);
    }

    @Test
    void leaveGroup_미납벌금이_있으면_예외() {
        GroupMember member = member(1, GroupRole.MEMBER);
        when(gmr.findByGroupIdAndUserIdAndIsActiveTrue(1, 100)).thenReturn(Optional.of(member));

        Todo expiredTodo = Todo.builder().status(com.example.commitmate.todo.TodoStatus.EXPIRED).build();
        Fine unpaidFine = Fine.builder().groupMember(member).status(FineStatus.UNPAID).build();
        unpaidFine.setTodo(expiredTodo);
        when(fr.findByGroupId(1)).thenReturn(List.of(unpaidFine));

        assertThatThrownBy(() -> groupService.leaveGroup(1, 100))
                .isInstanceOf(ExceptionFine.class);
    }
}