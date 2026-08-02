package com.example.commitmate.fine;

import com.example.commitmate.core.errors.Exception403;
import com.example.commitmate.core.errors.ExceptionFine;
import com.example.commitmate.core.errors.ExceptionNoInfo;
import com.example.commitmate.core.errors.ExceptionPoint;
import com.example.commitmate.group.Group;
import com.example.commitmate.group.GroupRepository;
import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.groupmember.GroupMemberRepository;
import com.example.commitmate.groupmember.GroupRole;
import com.example.commitmate.user.User;
import com.example.commitmate.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FineServiceTest {

    @Mock
    private FineRepository fr;
    @Mock
    private GroupMemberRepository gmr;
    @Mock
    private UserRepository ur;
    @Mock
    private GroupRepository gr;

    @InjectMocks
    private FineService fineService;

    private User user;
    private Group group;
    private GroupMember groupMember;
    private Fine fine;

    private void setUpUnpaidFine(int userPoint, int fineAmount) {
        user = new User();
        user.setId(10);
        user.setPoint(userPoint);

        group = Group.builder().roomName("스터디").build();
        group.setId(1);

        groupMember = GroupMember.builder().user(user).group(group).role(GroupRole.MEMBER).build();
        groupMember.setId(1);

        fine = Fine.builder().amount(fineAmount).groupMember(groupMember).status(FineStatus.UNPAID).build();
        fine.setId(100);
    }

    @Test
    void payFine_성공시_포인트가_차감되고_그룹포인트로_적립된다() {
        setUpUnpaidFine(5000, 1000);
        when(fr.findByIdWithMember(100)).thenReturn(Optional.of(fine));
        when(ur.findById(10)).thenReturn(Optional.of(user));

        fineService.payFine(100, 10, "완납했습니다");

        assertThat(user.getPoint()).isEqualTo(4000);
        assertThat(group.getGroupPoint()).isEqualTo(1000);
        assertThat(fine.getStatus()).isEqualTo(FineStatus.PAID);
        assertThat(fine.getMemo()).isEqualTo("완납했습니다");
    }

    @Test
    void payFine_존재하지_않으면_예외() {
        when(fr.findByIdWithMember(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fineService.payFine(999, 10, null))
                .isInstanceOf(ExceptionNoInfo.class);
    }

    @Test
    void payFine_본인_벌금이_아니면_예외() {
        setUpUnpaidFine(5000, 1000);
        when(fr.findByIdWithMember(100)).thenReturn(Optional.of(fine));

        assertThatThrownBy(() -> fineService.payFine(100, 999, null))
                .isInstanceOf(Exception403.class);
    }

    @Test
    void payFine_이미_납부완료면_예외() {
        setUpUnpaidFine(5000, 1000);
        fine.setStatus(FineStatus.PAID);
        when(fr.findByIdWithMember(100)).thenReturn(Optional.of(fine));

        assertThatThrownBy(() -> fineService.payFine(100, 10, null))
                .isInstanceOf(ExceptionFine.class);
    }

    @Test
    void payFine_포인트가_부족하면_예외() {
        setUpUnpaidFine(500, 1000);
        when(fr.findByIdWithMember(100)).thenReturn(Optional.of(fine));
        when(ur.findById(10)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> fineService.payFine(100, 10, null))
                .isInstanceOf(ExceptionPoint.class);
    }

    @Test
    void isAdmin_관리자면_true() {
        setUpUnpaidFine(5000, 1000);
        groupMember.setRole(GroupRole.ADMIN);
        when(gmr.findByGroupIdAndUserIdAndIsActiveTrue(1, 10)).thenReturn(Optional.of(groupMember));

        assertThat(fineService.isAdmin(1, 10)).isTrue();
    }

    @Test
    void isAdmin_그룹멤버가_아니면_false() {
        when(gmr.findByGroupIdAndUserIdAndIsActiveTrue(1, 10)).thenReturn(Optional.empty());

        assertThat(fineService.isAdmin(1, 10)).isFalse();
    }
}