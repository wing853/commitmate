package com.example.commitmate.fine;

import com.example.commitmate.core.errors.*;
import com.example.commitmate.group.Group;
import com.example.commitmate.group.GroupRepository;
import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.groupmember.GroupMemberRepository;
import com.example.commitmate.groupmember.GroupRole;
import com.example.commitmate.user.User;
import com.example.commitmate.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FineService {
    private final FineRepository fr;
    private final GroupMemberRepository gmr;
    private final UserRepository ur;
    private final GroupRepository gr;

    public List<Fine> findAllFine(Integer groupId) {
        List<Fine> fines = fr.findByGroupId(groupId);
        return fines;
    }


    public FineResponse.GroupFineInfo getFineInfo(Integer groupId) {
        List<Fine> fineList = findAllFine(groupId);
        List<Fine> expiredFines = fineList.stream()
                .filter(Fine::isExpired)
                .collect(Collectors.toList());

        List<Fine> withdrawnFines = fr.findByGroupMemberGroupIdAndTodoIsNull(groupId)
                .stream()
                .filter(f -> f.getStatus() != FineStatus.UNPAID)
                .collect(Collectors.toList());

        expiredFines.addAll(withdrawnFines);

        int unpaidCount = (int) expiredFines.stream().filter(Fine::isUnpaid).count();
        int paidCount = (int) expiredFines.stream().filter(Fine::isPaid).count();

        int unpaidAmount = expiredFines.stream().filter(Fine::isUnpaid).mapToInt(Fine::getAmount).sum();
        int paidAmount = expiredFines.stream().filter(Fine::isPaid).mapToInt(Fine::getAmount).sum();

        int totalAmount = unpaidAmount;

        return FineResponse.GroupFineInfo.builder()
                .expiredFines(expiredFines)
                .totalFinesAmount(totalAmount)
                .unpaidCount(unpaidCount)
                .paidCount(paidCount)
                .unpaidAmount(unpaidAmount)
                .paidAmount(paidAmount)
                .build();
    }

    @Transactional
    public void payFine(Integer fineId, Integer userId, String memo) {
        Fine fine = fr.findByIdWithMember(fineId).orElseThrow(
                () -> new ExceptionNoInfo("벌금 내역을 찾을 수 없습니다.")
        );

        if(!fine.getGroupMember().getUser().getId().equals(userId)) {
            throw  new Exception403("본인이 납부할 금액이 아닙니다");
        }

        if(!fine.isUnpaid()) {
            throw new ExceptionFine("선택한 벌금은 납부가 완료된 상태입니다");
        }

        User user = ur.findById(userId).orElseThrow(
                () -> new ExceptionNoInfo("사용자를 찾을 수 없습니다")
        );
        if(user.getPoint() < fine.getAmount()) {
            throw new ExceptionPoint("포인트가 부족합니다. 현재 보유 포인트: " + user.getPoint() + "P");
        }

        user.setPoint(user.getPoint()-fine.getAmount());

        Group group = fine.getGroupMember().getGroup();
        group.setGroupPoint(group.getGroupPoint() + fine.getAmount());

        fine.setStatus(FineStatus.PAID);
        fine.setMemo(memo);
    }

    public boolean isAdmin(Integer groupId, Integer userId) {
        return gmr.findByGroupIdAndUserIdAndIsActiveTrue(groupId, userId) // 수정
                .map(m -> m.getRole() == GroupRole.ADMIN)
                .orElse(false);
    }

    public Group getGroup(Integer groupId) {
        return gr.findById(groupId).orElseThrow(
                () -> new ExceptionNoInfo("그룹을 찾을 수 없습니다.")
        );
    }
}
