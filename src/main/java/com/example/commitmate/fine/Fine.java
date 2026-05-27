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
    @Enumerated(EnumType.STRING)
    private FineStatus status;
    private String memo;
    // 생성 시간 (SQL의 created_at)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_member_id") // 외래키 컬럼명 명시
    private GroupMember groupMember;

    @OneToOne(mappedBy = "fine")
    private Todo todo;

    @Builder
    public Fine(Integer amount, FineStatus status, LocalDateTime createdAt, GroupMember groupMember) {
        if(amount == null) {
            amount = 0;
        }
        this.amount = amount;
        this.status = (status == null) ? FineStatus.UNPAID:status;
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        this.createdAt = createdAt;
        this.groupMember = groupMember;
    }

    public boolean isHasAmount() {
        return this.amount != null && this.amount > 0;
    }


    // Fine.java

    public boolean isExpired() {
        if (this.todo == null) return false;
        return this.todo.isExpired(); // Todo의 판단 로직을 그대로 사용
    }

    public Integer getFineId(){
        return this.id;
    }

    public boolean isUnpaid()  { return this.status == FineStatus.UNPAID; }
    public boolean isPaid()    { return this.status == FineStatus.PAID; }
}