package com.example.commitmate.todo;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo,Integer> {

    @Query("""
            select t from Todo t
            join fetch t.groupMember gm
            join fetch gm.user
            join fetch gm.group
            where gm.group.id = :groupId
            """)
    List<Todo> findByGroupIdWithMember(Integer groupId);

    @Modifying
    @Transactional
    void deleteByGroupMember_Group_Id(Integer groupId);

    List<Todo> findByGroupMember_Group_IdAndGroupMember_User_Id(Integer groupId, Integer userId);
}
