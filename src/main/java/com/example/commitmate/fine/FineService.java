package com.example.commitmate.fine;

import com.example.commitmate.core.errors.Exception400;
import com.example.commitmate.core.errors.Exception403;
import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.groupmember.GroupMemberRepository;
import com.example.commitmate.groupmember.GroupRole;
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
        int pendingCount = (int) expiredFines.stream().filter(Fine::isPending).count();
        int paidCount = (int) expiredFines.stream().filter(Fine::isPaid).count();

        int unpaidAmount = expiredFines.stream().filter(Fine::isUnpaid).mapToInt(Fine::getAmount).sum();
        int pendingAmount = expiredFines.stream().filter(Fine::isPending).mapToInt(Fine::getAmount).sum();
        int paidAmount = expiredFines.stream().filter(Fine::isPaid).mapToInt(Fine::getAmount).sum();

        int totalAmount = unpaidAmount + pendingAmount;

        return FineResponse.GroupFineInfo.builder()
                .expiredFines(expiredFines)
                .totalFinesAmount(totalAmount)
                .unpaidCount(unpaidCount)
                .pendingCount(pendingCount)
                .paidCount(paidCount)
                .unpaidAmount(unpaidAmount)
                .pendingAmount(pendingAmount)
                .paidAmount(paidAmount)
                .build();
    }

    @Transactional
    public void payFine(Integer fineId, Integer userId, String memo) {
        Fine fine = fr.findByIdWithMember(fineId).orElseThrow(
                () -> new Exception400("벌금 내역을 찾을 수 없습니다.")
        );

        if(!fine.getGroupMember().getUser().getId().equals(userId)) {
            throw  new Exception403("본인이 납부할 금액이 아닙니다");
        }

        if(!fine.isUnpaid()) {
            throw new Exception400("선택한 벌금은 납부 승인 대기중이거나 납부가 완료된 상태입니다");
        }

        fine.setStatus(FineStatus.PENDING);
        fine.setMemo(memo);
    }

    @Transactional
    public void approveFine(Integer fineId, Integer userId, Integer groupId) {
        Fine fine = fr.findByIdWithMember(fineId).orElseThrow(
                () -> new Exception400("벌금 내역을 찾을 수 없습니다.")
        );

        GroupMember member = gmr.findByGroupIdAndUserIdAndIsActiveTrue(groupId, userId).orElseThrow( // 수정
                () -> new Exception403("그룹 멤버가 아닙니다.")
        );

        if (!member.isAdmin()) {
            throw new Exception403("벌금 승인은 총무만 진행 가능합니다.");
        }

        if (fine.isUnpaid()) {
            throw new Exception400("선택한 벌금은 아직 인증 되지 않은 상태입니다.");
        }

        if (fine.isPaid()) {
            throw new Exception400("이미 납부가 완료된 벌금입니다.");
        }

        fine.setStatus(FineStatus.PAID);
    }


    public boolean isAdmin(Integer groupId, Integer userId) {
        return gmr.findByGroupIdAndUserIdAndIsActiveTrue(groupId, userId) // 수정
                .map(m -> m.getRole() == GroupRole.ADMIN)
                .orElse(false);
    }
}
