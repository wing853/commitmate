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


    public FineResponse.GroupFineInfo getFineInfo(Integer id) {
        List<Fine> fineList = findAllFine(id);
        List<Fine> expiredFines = fineList.stream()
                .filter(Fine::isExpired) // Todo의 마감 기한이 지난 것만 필터링
                .collect(Collectors.toList());
        Integer totalAmount = expiredFines.stream()
                .mapToInt(Fine::getAmount)
                .sum();

        return FineResponse.GroupFineInfo.builder()
                .expiredFines(expiredFines)
                .totalFinesAmount(totalAmount)
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

        GroupMember member = gmr.findByGroupIdAndUserIdAndIsActiveTrue(groupId, userId).orElseThrow(
                () -> new Exception403("그룹 멤버가 아닙니다.")
        );

        if(!member.isAdmin()) {
            // todo: 총무 벌금 납부는 추후 개발 예정
            throw new Exception403("벌금 승인은 총무만 진행 가능합니다.");
        }

        if(fine.isUnpaid()) {
            throw new Exception400("선택한 벌금은 아직 인증 되지 않은 상태입니다.");
        }

        if (fine.isPaid()) {
            throw new Exception400("이미 납부가 완료된 벌금입니다.");
        }

        fine.setStatus(FineStatus.PAID);
    }

    public boolean isAdmin(Integer groupId, Integer userId) {
        return gmr.findByGroupIdAndUserIdAndIsActiveTrue(groupId, userId)
                .map(m -> m.getRole() == GroupRole.ADMIN)
                .orElse(false);
    }
}
