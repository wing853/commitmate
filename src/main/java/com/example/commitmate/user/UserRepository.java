package com.example.commitmate.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    @Query("""
            select u from User u where u.email = :email and u.password = :password
            """)
    Optional<User> findByEmailAndPassword(@Param("email") String email,
                                          @Param("password") String password);

    Optional<User> findByNickname(String nickname);

}
