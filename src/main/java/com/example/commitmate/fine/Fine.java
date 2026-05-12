package com.example.commitmate.fine;

import com.example.commitmate.groupmember.GroupMember;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name="fine_tb")
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
    @Column(nullable = false)
    private String status;

    // 생성 시간 (SQL의 created_at)
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_member_id") // 외래키 컬럼명 명시
    private GroupMember groupMember;

    @Builder
    public Fine(Integer amount, String status, LocalDateTime createdAt, GroupMember groupMember) {
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.groupMember = groupMember;
    }
}