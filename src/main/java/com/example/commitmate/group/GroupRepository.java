package com.example.commitmate.group;

import com.example.commitmate.groupmember.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group,Integer> {

    Optional<Group> findByInviteCode(String inviteCode);
    Optional<Group> findByJoinCode(String joinCode);


}
