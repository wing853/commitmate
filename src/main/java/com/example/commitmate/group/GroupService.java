package com.example.commitmate.group;

import com.example.commitmate.core.errors.Exception400;
import com.example.commitmate.core.errors.Exception403;
import com.example.commitmate.fine.FineRepository;
import com.example.commitmate.fine.FineStatus;
import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.groupmember.GroupMemberRepository;
import com.example.commitmate.groupmember.GroupRole;
import com.example.commitmate.todo.TodoRepository;
import com.example.commitmate.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository gr;
    private final GroupMemberRepository gmr;
    private final TodoRepository tr;
    private final FineRepository fr;

    @Transactional
    public List<GroupResponse.MyGroupDTO> getMyGroups(Integer userId) {
        // 1. 내가 가입한 그룹 엔티티 리스트만 조회
        List<GroupMember> members = gmr.findByUserId(userId);

        // 2. DTO로 변환
        return members.stream()
                .map(m -> {
                    // [수정] 문자열 "ADMIN"이 아니라 Enum 상수와 직접 비교해야 합니다.
                    // GroupRole.ADMIN 또는 GroupRole.valueOf("ADMIN") 등을 사용하세요.
                    boolean isManager = (m.getRole() == GroupRole.ADMIN);

                    return new GroupResponse.MyGroupDTO(m.getGroup(), isManager);
                })
                .collect(Collectors.toList());
    }

    @Transactional // 데이터 변경이 일어나므로 트랜잭션 처리가 필수입니다.
    public Group createRoom(User sessionUser, GroupRequest.CreateDTO createDTO) {

        // 1. 그룹 엔티티 생성 및 저장
        Group groupEntity = createDTO.toEntity();
        gr.save(groupEntity); // 여기서 ID가 생성됨

        // 2. 방장을 GroupMember로 등록 (이걸 안 하면 목록에 안 뜹니다!)
        GroupMember manager = createDTO.toMemberEntity(sessionUser,groupEntity);
        gmr.save(manager); // GroupMemberRepository를 통해 저장

        return groupEntity;
    }

    // 초대링크로 참여
    @Transactional
    public void joinByInviteCode(String inviteCode, User sessionUser,
                                 GroupRequest.JoinDTO joinDTO) {
        Group group = gr.findByInviteCode(inviteCode).orElseThrow(
                () -> new Exception400("유효하지 않은 초대링크입니다.")
        );

        gmr.findByGroupIdAndUserId(group.getId(), sessionUser.getId())
                .ifPresent(m -> { throw new Exception400("이미 참여 중인 그룹입니다."); });

        GroupMember member = joinDTO.toEntity(sessionUser,group);
        gmr.save(member);
    }

    // 가입코드로 참여
    @Transactional
    public void joinByJoinCode(String joinCode, User sessionUser,
                               GroupRequest.JoinDTO joinDTO) {
        Group group = gr.findByJoinCode(joinCode).orElseThrow(
                () -> new Exception400("유효하지 않은 가입코드입니다.")
        );

        gmr.findByGroupIdAndUserId(group.getId(), sessionUser.getId())
                .ifPresent(m -> { throw new Exception400("이미 참여 중인 그룹입니다."); });

        GroupMember member = joinDTO.toEntity(sessionUser,group);
        gmr.save(member);
    }

    // todo-list에서 초대 정보 조회
    public GroupResponse.InviteInfoDTO getInviteInfo(Integer groupId) {
        Group group = gr.findById(groupId).orElseThrow(
                () -> new Exception400("해당 그룹을 찾을 수 없습니다.")
        );
        return new GroupResponse.InviteInfoDTO(group);
    }

    public GroupResponse.detailDTO findGroupById(Integer id) {
        Group group = gr.findById(id).orElseThrow(
                () -> new Exception400("해당 그룹을 찾을 수 없습니다")
        );

        return new GroupResponse.detailDTO(group);
    }

    @Transactional
    public void updateGroup(Integer id, GroupRequest.UpdateDTO updateDTO) {
        Group group = gr.findById(id).orElseThrow(
                () -> new Exception400("해당 그룹을 찾을 수 없습니다.")
        );

        group.update(updateDTO.getRoomName(),updateDTO.getDescription());
    }

    @Transactional
    public void deleteGroup(Integer groupId, Integer userId) {

        Group group = gr.findById(groupId).orElseThrow(
                () -> new Exception400("해당 그룹을 찾을 수 없습니다.")
        );

        GroupMember member = gmr.findByGroupIdAndUserId(groupId,userId).orElseThrow(
                () -> new Exception403("해당 그룹의 멤버가 아닙니다.(그룹을 확인하세요.)")
        );

        if(member.getRole() != GroupRole.ADMIN) {
            throw new Exception403("그룹 삭제 권한이 없습니다.");
        }

        tr.deleteByGroupMember_Group_Id(groupId);
        gmr.deleteByGroupId(groupId);
        gr.delete(group);
    }

    public List<GroupResponse.MemberDTO> getMembers(Integer groupId) {
        List<GroupMember> members = gmr.findByGroupId(groupId);
        return members.stream()
                .map(GroupResponse.MemberDTO::new)
                .collect(Collectors.toList());
    }

    public boolean isAdmin(Integer groupId, Integer userId) {
        return gmr.findByGroupIdAndUserId(groupId, userId)
                .map(m -> m.getRole() == GroupRole.ADMIN)
                .orElse(false);
    }

    @Transactional
    public void kickMember(Integer groupId, Integer groupMemberId, Integer requestUserId) {
        // 요청자가 관리자인지 확인
        GroupMember requester = gmr.findByGroupIdAndUserId(groupId, requestUserId)
                .orElseThrow(() -> new Exception403("권한이 없습니다."));

        if (requester.getRole() != GroupRole.ADMIN) {
            throw new Exception403("관리자만 강퇴할 수 있습니다.");
        }

        GroupMember target = gmr.findById(groupMemberId)
                .orElseThrow(() -> new Exception400("해당 멤버를 찾을 수 없습니다."));

        // 관리자는 강퇴 불가
        if (target.getRole() == GroupRole.ADMIN) {
            throw new Exception403("관리자는 강퇴할 수 없습니다.");
        }

        // 미납 벌금 확인 (UNPAID 또는 PENDING 상태)
        boolean hasUnpaidFine = target.getUser().getId() != null &&
                fr.findByGroupId(groupId).stream()
                        .filter(f -> f.getGroupMember().getId().equals(groupMemberId))
                        .filter(f->f.isExpired())
                        .anyMatch(f -> f.getStatus() == FineStatus.UNPAID
                                || f.getStatus() == FineStatus.PENDING);

        if (hasUnpaidFine) {
            throw new Exception400("미납 또는 승인 대기 중인 벌금이 있어 강퇴할 수 없습니다.");
        }

        tr.deleteByGroupMemberId(groupMemberId);

        gmr.delete(target);
    }
}
