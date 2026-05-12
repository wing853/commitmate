package com.example.commitmate.groupmember;

import com.example.commitmate.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {
    @Query("""
            select gm from GroupMember gm join fetch gm.group where gm.user.id = :userId
            """)
    List<GroupMember> findByUserId(@Param("userId") Integer userId);
}
