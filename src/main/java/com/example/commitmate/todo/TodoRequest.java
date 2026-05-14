package com.example.commitmate.todo;


import com.example.commitmate.fine.Fine;
import com.example.commitmate.group.Group;
import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.user.User;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TodoRequest {

    @Data
    public static class AddDTO {
        private String work;    // 할 일 내용
        private Integer userId;  // 담당자 ID
        private Integer groupId; // 속한 그룹 ID
        private Integer amount;
        private String deadline;

        // 빌더 패턴을 사용할 엔티티 변환 메서드
        // fine은 아직 없으므로 파라미터에서 제외하거나 null로 설정
        public Todo toEntity(GroupMember groupMember) {
            Fine newFine = Fine.builder()
                    .amount(this.amount)
                    .build();

            Timestamp tsDeadline = null;
            if(deadline!=null && !deadline.isBlank()) {
                try {
                    String formatted = this.deadline.replace("T", " ");
                    if (formatted.length() == 16) formatted += ":00";
                    tsDeadline = java.sql.Timestamp.valueOf(formatted);
                } catch (Exception e) {
                    System.out.println("날짜 변환 에러: " + e.getMessage());
                }
            }

            return Todo.builder()
                    .work(this.work)
                    .groupMember(groupMember)
                    .fine(newFine)
                    .status(TodoStatus.READY) // 생성 시 기본값
                    .deadline(tsDeadline)
                    .build();
        }
    }
}
