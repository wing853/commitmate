package com.example.commitmate.todo;


import com.example.commitmate.core.errors.Exception500;
import com.example.commitmate.fine.Fine;
import com.example.commitmate.groupmember.GroupMember;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

public class TodoRequest {

    @Data
    public static class AddDTO {
        private String work;    // 할 일 내용
        private Integer userId;  // 담당자 ID
        private Integer groupId; // 속한 그룹 ID
        private Integer amount;
        private String deadline;
        private List<Integer> memberIds; // 단체일정 대상 그룹멤버 id 목록 (개인일정이면 null/빈 값)

        // 빌더 패턴을 사용할 엔티티 변환 메서드
        // fine은 아직 없으므로 파라미터에서 제외하거나 null로 설정
        public Todo toEntity(GroupMember groupMember) {
            return toEntity(groupMember, null);
        }

        public Todo toEntity(GroupMember groupMember, String batchId) {
            Fine newFine = Fine.builder()
                    .amount(this.amount)
                    .groupMember(groupMember)
                    .build();

            Timestamp tsDeadline = null;
            if(deadline!=null && !deadline.isBlank()) {
                try {
                    String formatted = this.deadline.replace("T", " ");
                    if (formatted.length() == 16) formatted += ":00";
                    tsDeadline = java.sql.Timestamp.valueOf(formatted);
                } catch (Exception e) {
                    throw new Exception500("관리자에게 문의하세요");
                }
            }

            return Todo.builder()
                    .work(this.work)
                    .groupMember(groupMember)
                    .fine(newFine)
                    .status(TodoStatus.READY) // 생성 시 기본값
                    .deadline(tsDeadline)
                    .batchId(batchId)
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    public static class UpdateTodoDTO {
        private String work;
        private Integer amount;
        private String deadline;

        public void applyTo(Todo todo) {
            if (work != null && !work.isBlank()) {
                todo.setWork(work);
            }

            if (deadline != null && !deadline.isBlank()) {
                try {
                    String formatted = deadline.replace("T", " ");
                    if (formatted.length() == 16) formatted += ":00";
                    todo.setDeadline(Timestamp.valueOf(formatted));
                    todo.setReminderSent(false);
                } catch (Exception e) {
                    throw new Exception500("날짜 형식이 올바르지 않습니다.");
                }
            } else {
                todo.setDeadline(null);
            }

            if (todo.getFine() != null && amount != null) {
                todo.getFine().setAmount(amount);
            }
        }
    }
}
