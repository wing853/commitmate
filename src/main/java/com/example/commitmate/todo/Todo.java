package com.example.commitmate.todo;

import com.example.commitmate.fine.Fine;
import com.example.commitmate.group.Group;
import com.example.commitmate.groupmember.GroupMember;
import com.example.commitmate.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Data
@NoArgsConstructor
@Entity
@Table(name = "todo_tb")
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String work;
    private boolean isDone;
    @CreationTimestamp
    private Timestamp createdAt;
    @CreationTimestamp
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

    public String getFormatDeadline() {
        if (createdAt == null) return "";
        return new SimpleDateFormat("yyyy.MM.dd HH:mm").format(deadline);
    }

    @Builder
    public Todo(Integer id, String work, boolean isDone, Timestamp createdAt,
                Timestamp deadline, GroupMember groupMember, Fine fine) {
        this.id = id;
        this.work = work;
        this.isDone = isDone;
        this.createdAt = createdAt;
        this.deadline = deadline;
        this.groupMember = groupMember;
        this.fine = fine;
    }
}
