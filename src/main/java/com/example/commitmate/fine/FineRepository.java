package com.example.commitmate.fine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FineRepository extends JpaRepository<Fine, Integer> {

    @Query("SELECT f FROM Fine f " +
            "JOIN FETCH f.todo t " +
            "JOIN FETCH t.groupMember gm " +
            "JOIN FETCH gm.user u " +
            "WHERE gm.group.id = :groupId")
    List<Fine> findByGroupId(@Param("groupId") Integer groupId);

    @Query("SELECT f FROM Fine f " +
            "JOIN FETCH f.groupMember gm " +
            "JOIN FETCH gm.user u " +
            "WHERE f.id = :fineId")
    Optional<Fine> findByIdWithMember(@Param("fineId") Integer fineId);
}
