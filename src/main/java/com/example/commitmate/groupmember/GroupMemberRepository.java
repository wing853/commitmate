package com.example.commitmate.groupmember;

import com.example.commitmate.group.Group;
import com.example.commitmate.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {
    @Query("""
            select gm from GroupMember gm join fetch gm.group where gm.user.id = :userId
            """)
    List<GroupMember> findByUserId(@Param("userId") Integer userId);

    @Modifying
    @Transactional
    void deleteByGroupId(Integer groupId);

    Optional<GroupMember> findByGroupIdAndUserId(Integer groupId, Integer userId);

}
