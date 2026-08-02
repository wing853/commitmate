package com.example.commitmate.group;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Entity
@Table(name = "group_tb")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String roomName;
    private String description;
    private String inviteCode;
    private String joinCode;
    @CreationTimestamp
    private Timestamp createdAt;
    @ColumnDefault("0")
    private Integer groupPoint = 0;

    @Builder
    public Group(String roomName, String description, String inviteCode,String joinCode) {
        this.roomName = roomName;
        this.description = description;
        this.inviteCode = inviteCode;
        this.joinCode = joinCode;
    }

    public void update(String roomName, String description) {
        this.roomName = roomName;
        this.description = description;
    }

}
