package com.example.commitmate.groupmember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {

    List<GroupMember> findByUserId(Integer userId);
    Optional<GroupMember> findByGroupIdAndUserId(Integer groupId, Integer userId);
    List<GroupMember> findByGroupId(Integer groupId);
    @Modifying
    @Transactional
    void deleteByGroupId(Integer groupId);



    List<GroupMember> findByUserIdAndIsActiveTrue(Integer userId);

    List<GroupMember> findByGroupIdAndIsActiveTrue(Integer groupId);

    Optional<GroupMember> findByGroupIdAndUserIdAndIsActiveTrue(Integer groupId, Integer userId);

}
