package com.example.commitmate.groupmember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> {

    List<GroupMember> findByUserId(Integer userId);

    @Modifying
    @Transactional
    void deleteByGroupId(Integer groupId);

    Optional<GroupMember> findByGroupIdAndUserId(Integer groupId, Integer userId);

}
