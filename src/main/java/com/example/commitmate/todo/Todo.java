package com.example.commitmate.todo;

import com.example.commitmate.fine.Fine;
import com.example.commitmate.groupmember.GroupMember;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "todo_tb")
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String work;
    @Enumerated(EnumType.STRING)
    private TodoStatus status;
    @CreationTimestamp
    private Timestamp createdAt;
    @Column(name = "deadline")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Timestamp deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_member_id")
    private GroupMember groupMember;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="fine_id")
    private Fine fine;

    public String getFormatCreatedAt() {
        if (createdAt == null) return "";
        return new SimpleDateFormat("yyyy.MM.dd HH:mm").format(createdAt);
    }

    public String getDeadlineForInput() {
        if (deadline == null) return "";

        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
                .format(deadline);
    }


    public String getFormatDeadline() {
        if (deadline == null){
            return "";
        }
        return new SimpleDateFormat("yyyy.MM.dd HH:mm").format(deadline);
    }

    @Builder
    public Todo(Integer id, String work, TodoStatus status, Timestamp createdAt,
                Timestamp deadline, GroupMember groupMember, Fine fine) {
        this.id = id;
        this.work = work;
        this.status = status;
        this.createdAt = createdAt;
        this.deadline = deadline;
        this.groupMember = groupMember;
        this.fine = fine;
    }

    public void updateStatus() {
        if (this.status == TodoStatus.FINISH) {
            return;
        }

        if (this.deadline != null &&
                LocalDateTime.now().isAfter(this.deadline.toLocalDateTime())) {

            this.status = TodoStatus.EXPIRED;
        }
    }

    public boolean isComplete() {
        return this.status == TodoStatus.FINISH;
    }

    public boolean isReady() {
        return (this.status != TodoStatus.FINISH) && !this.isExpired();
    }

    public boolean isExpired() {
        // 1. 이미 완료(FINISH)된 상태라면 절대 만료가 아님
        if (this.status == TodoStatus.FINISH) return false;

        // 2. 이미 상태가 EXPIRED이거나,
        //    READY 상태지만 실시간으로 시간이 지났다면 마감으로 판단
        boolean isTimeOver = this.deadline != null &&
                LocalDateTime.now().isAfter(this.deadline.toLocalDateTime());

        return this.status == TodoStatus.EXPIRED || isTimeOver;
    }

    // 프로그레스 바용 진행률 계산 (0~100)
    public int getDeadlineProgress() {
        if (createdAt == null || deadline == null) return 0;
        long total = deadline.getTime() - createdAt.getTime();
        long elapsed = System.currentTimeMillis() - createdAt.getTime();
        if (total <= 0) return 100;
        int progress = (int) ((elapsed * 100) / total);
        return Math.min(progress, 100);
    }

    public String getProgressColor() {
        int p = getDeadlineProgress();
        if (p >= 80) return "#f87171";
        if (p >= 50) return "#f59e0b";
        return "#34d399";
    }

}
