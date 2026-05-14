package com.example.commitmate.fine;

import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.todo.Todo;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fine_tb")
@Data
@NoArgsConstructor
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 벌금 액수 (SQL의 amount)
    @Column(nullable = false)
    private Integer amount;

    // 벌금 상태 (SQL의 status - 예: UNPAID, PAID)
    private String status;

    // 생성 시간 (SQL의 created_at)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_member_id") // 외래키 컬럼명 명시
    private GroupMember groupMember;

    @OneToOne(mappedBy = "fine")
    private Todo todo;

    @Builder
    public Fine(Integer amount, String status, LocalDateTime createdAt, GroupMember groupMember) {
        if(amount == null) {
            amount = 0;
        }
        this.amount = amount;
        if (status == null) {
            status = "UNPAID";
        }
        this.status = status;
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        this.createdAt = createdAt;
        this.groupMember = groupMember;
    }

    public boolean isHasAmount() {
        return this.amount != null && this.amount > 0;
    }

    public boolean isExpired() {
        return (this.todo != null &&
                this.todo.getDeadline() != null &&
                LocalDateTime.now().isAfter(this.todo.getDeadline().toLocalDateTime()));
    }
}