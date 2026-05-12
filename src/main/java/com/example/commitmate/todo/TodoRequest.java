package com.example.commitmate.todo;


import com.example.commitmate.fine.Fine;
import com.example.commitmate.group.Group;
import com.example.commitmate.user.User;
import lombok.Builder;
import lombok.Data;

public class TodoRequest {

    @Data
    public static class AddDTO {
        private String work;    // 할 일 내용
        private Integer userId;  // 담당자 ID
        private Integer groupId; // 속한 그룹 ID

        // 빌더 패턴을 사용할 엔티티 변환 메서드
        // fine은 아직 없으므로 파라미터에서 제외하거나 null로 설정
        public Todo toEntity(User user, Group group) {
            return Todo.builder()
                    .work(this.work)
                    .user(user)
                    .group(group)
                    .isDone(false) // 생성 시 기본값
                    .fine(null)    // 아직 벌금 기능이 없으므로 null
                    .build();
        }
    }
}
