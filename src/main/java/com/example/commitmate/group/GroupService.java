package com.example.commitmate.group;

import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.groupmember.GroupMemberRepository;
import com.example.commitmate.groupmember.GroupRole;
import com.example.commitmate.user.User;
import jakarta.servlet.http.HttpSession;
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
        GroupMember manager = GroupMember.builder()
                .user(sessionUser)
                .group(groupEntity)
                .role(GroupRole.ADMIN)
                .achievementRate(0.0)
                .build();
                new GroupMember();
        gmr.save(manager); // GroupMemberRepository를 통해 저장

        return groupEntity;
    }

    public GroupResponse.UpdateDTO findGroupById(Integer id) {
        Group group = gr.findById(id).orElseThrow(
                () -> new RuntimeException("해당 그룹을 찾을 수 없습니다")
        );

        return new GroupResponse.UpdateDTO(group);
    }

    @Transactional
    public void updateGroup(Integer id, GroupRequest.UpdateDTO updateDTO) {
        Group group = gr.findById(id).orElseThrow(
                () -> new RuntimeException("해당 그룹을 찾을 수 없습니다.")
        );

        group.update(updateDTO.getRoomName(),updateDTO.getDescription());
    }
}
