package com.example.commitmate.todo;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo,Integer> {

    List<Todo> findByGroupId(Integer groupId);

    @Modifying
    @Transactional
    void deleteByGroupId(Integer groupId);
}
