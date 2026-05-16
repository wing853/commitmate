package com.example.commitmate.groupmember;

import com.example.commitmate.group.Group;
import com.example.commitmate.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "group_member_tb")
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    private Group group;
    @Enumerated(EnumType.STRING)
    private GroupRole role;


    @Builder
    public GroupMember(Integer id, User user, Group group, GroupRole role) {
        this.id = id;
        this.user = user;
        this.group = group;
        this.role = role;

    }

    public boolean isAdmin() {
        return role == GroupRole.ADMIN;
    }
}
