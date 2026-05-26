package com.example.commitmate.group;

import com.example.commitmate.core.errors.Exception400;
import com.example.commitmate.core.errors.Exception403;
import com.example.commitmate.fine.Fine;
import com.example.commitmate.fine.FineRepository;
import com.example.commitmate.fine.FineStatus;
import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.groupmember.GroupMemberRepository;
import com.example.commitmate.groupmember.GroupRole;
import com.example.commitmate.todo.Todo;
import com.example.commitmate.todo.TodoRepository;
import com.example.commitmate.user.User;
import jakarta.persistence.EntityManager;
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
    private final EntityManager em;

    @Transactional
    public List<GroupResponse.MyGroupDTO> getMyGroups(Integer userId) {
        List<GroupMember> members = gmr.findByUserIdAndIsActiveTrue(userId);
        return members.stream()
                .map(m -> {
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
    public void joinByInviteCode(String inviteCode, User sessionUser) {
        Group group = gr.findByInviteCode(inviteCode).orElseThrow(
                () -> new Exception400("유효하지 않은 초대링크입니다.")
        );
        gmr.findByGroupIdAndUserIdAndIsActiveTrue(group.getId(), sessionUser.getId())
                .ifPresent(m -> { throw new Exception400("이미 참여 중인 그룹입니다."); });

        GroupMember member = GroupMember.builder()
                .user(sessionUser)
                .group(group)
                .role(GroupRole.MEMBER)
                .build();
        gmr.save(member);
    }

    // 가입코드로 참여
    @Transactional
    public void joinByJoinCode(String joinCode, User sessionUser) {
        Group group = gr.findByJoinCode(joinCode).orElseThrow(
                () -> new Exception400("유효하지 않은 가입코드입니다.")
        );
        gmr.findByGroupIdAndUserIdAndIsActiveTrue(group.getId(), sessionUser.getId())
                .ifPresent(m -> { throw new Exception400("이미 참여 중인 그룹입니다."); });

        GroupMember member = GroupMember.builder()
                .user(sessionUser)
                .group(group)
                .role(GroupRole.MEMBER)
                .build();
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
        GroupMember member = gmr.findByGroupIdAndUserIdAndIsActiveTrue(groupId, userId).orElseThrow(
                () -> new Exception403("해당 그룹의 멤버가 아닙니다.")
        );
        if (member.getRole() != GroupRole.ADMIN) {
            throw new Exception403("그룹 삭제 권한이 없습니다.");
        }
        tr.deleteByGroupMember_Group_Id(groupId);
        gmr.deleteByGroupId(groupId);
        gr.delete(group);
    }

    public List<GroupResponse.MemberDTO> getMembers(Integer groupId) {
        List<GroupMember> members = gmr.findByGroupIdAndIsActiveTrue(groupId);
        return members.stream()
                .map(GroupResponse.MemberDTO::new)
                .collect(Collectors.toList());
    }

    public boolean isAdmin(Integer groupId, Integer userId) {
        return gmr.findByGroupIdAndUserIdAndIsActiveTrue(groupId, userId)
                .map(m -> m.getRole() == GroupRole.ADMIN)
                .orElse(false);
    }


    @Transactional
    public void kickMember(Integer groupId, Integer groupMemberId, Integer requestUserId) {
        GroupMember requester = gmr.findByGroupIdAndUserIdAndIsActiveTrue(groupId, requestUserId)
                .orElseThrow(() -> new Exception403("권한이 없습니다."));

        if (requester.getRole() != GroupRole.ADMIN) {
            throw new Exception403("관리자만 강퇴할 수 있습니다.");
        }

        GroupMember target = gmr.findById(groupMemberId)
                .orElseThrow(() -> new Exception400("해당 멤버를 찾을 수 없습니다."));

        if (target.getRole() == GroupRole.ADMIN) {
            throw new Exception403("관리자는 강퇴할 수 없습니다.");
        }

        // 미납 벌금 확인 (만료된 미션만)
        boolean hasUnpaidFine = fr.findByGroupId(groupId).stream()
                .filter(f -> f.getGroupMember() != null)
                .filter(f -> f.getGroupMember().getId().equals(groupMemberId))
                .filter(Fine::isExpired)
                .anyMatch(f -> f.getStatus() == FineStatus.UNPAID
                        || f.getStatus() == FineStatus.PENDING);

        if (hasUnpaidFine) {
            throw new Exception400("미납 또는 승인 대기 중인 벌금이 있어 강퇴할 수 없습니다.");
        }

        // Fine의 todo 연결 끊기 (JPQL로 직접 처리)
        List<Todo> todos = tr.findByGroupMemberId(groupMemberId);
        todos.forEach(todo -> {
            if (todo.getFine() != null) {
                todo.getFine().setTodo(null);
                todo.setFine(null); // fine_id = null로 만들기
            }
        });
        tr.detachFineByGroupMemberId(groupMemberId);

        // Todo 삭제
        tr.deleteByGroupMemberId(groupMemberId);

        // 멤버 소프트 삭제
        target.setActive(false);
    }

    @Transactional
    public void leaveGroup(Integer groupId, Integer userId) {
        GroupMember member = gmr.findByGroupIdAndUserIdAndIsActiveTrue(groupId, userId)
                .orElseThrow(() -> new Exception400("그룹 멤버가 아닙니다."));

        if (member.getRole() == GroupRole.ADMIN) {
            throw new Exception400("총무는 그룹을 나갈 수 없습니다. 총무를 위임한 후 나가주세요.");
        }

        // 미납 벌금 확인
        boolean hasUnpaidFine = fr.findByGroupId(groupId).stream()
                .filter(f -> f.getGroupMember() != null)
                .filter(f -> f.getGroupMember().getId().equals(member.getId()))
                .filter(Fine::isExpired)
                .anyMatch(f -> f.getStatus() == FineStatus.UNPAID
                        || f.getStatus() == FineStatus.PENDING);

        if (hasUnpaidFine) {
            throw new Exception400("미납 또는 승인 대기 중인 벌금이 있어 나갈 수 없습니다.");
        }

        // Fine의 todo 연결 끊기
        List<Todo> todos = tr.findByGroupMemberId(member.getId());
        todos.forEach(todo -> {
            if (todo.getFine() != null) {
                todo.getFine().setTodo(null);
                todo.setFine(null);
            }
        });
        tr.detachFineByGroupMemberId(member.getId());

        // Todo 삭제
        tr.deleteByGroupMemberId(member.getId());

        // 소프트 삭제
        member.setActive(false);
    }
}
