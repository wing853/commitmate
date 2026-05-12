package com.example.commitmate.todo;

import com.example.commitmate.fine.Fine;
import com.example.commitmate.group.Group;
import com.example.commitmate.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="fine_id")
    private Fine fine;

    public String getFormatCreatedAt() {
        if (createdAt == null) return "";
        return new SimpleDateFormat("yyyy.MM.dd HH:mm").format(createdAt);
    }

    @Builder
    public Todo(Integer id, String work, boolean isDone, Timestamp createdAt, Group group, User user, Fine fine) {
        this.id = id;
        this.work = work;
        this.isDone = isDone;
        this.createdAt = createdAt;
        this.group = group;
        this.user = user;
        this.fine = fine;
    }
}
