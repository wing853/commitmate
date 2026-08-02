package com.example.commitmate.fine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FineRepository extends JpaRepository<Fine, Integer> {

    @Query("SELECT f FROM Fine f " +
            "LEFT JOIN FETCH f.todo t " +  // INNER JOIN → LEFT JOIN으로 변경!
            "LEFT JOIN FETCH t.groupMember gm " +
            "LEFT JOIN FETCH gm.user u " +
            "WHERE f.groupMember.id IN " +
            "(SELECT gm2.id FROM GroupMember gm2 WHERE gm2.group.id = :groupId)")
    List<Fine> findByGroupId(@Param("groupId") Integer groupId);

    @Query("SELECT f FROM Fine f " +
            "JOIN FETCH f.groupMember gm " +
            "JOIN FETCH gm.user u " +
            "WHERE f.id = :fineId")
    Optional<Fine> findByIdWithMember(@Param("fineId") Integer fineId);

    @Query("SELECT f FROM Fine f " +
            "WHERE f.groupMember.group.id = :groupId " +
            "AND f.todo IS NULL")
    List<Fine> findByGroupMemberGroupIdAndTodoIsNull(@Param("groupId") Integer groupId);

    @Modifying
    @Query("UPDATE Fine f SET f.todo = null WHERE f.groupMember.id = :groupMemberId")
    void detachTodoByGroupMemberId(@Param("groupMemberId") Integer groupMemberId);
}
