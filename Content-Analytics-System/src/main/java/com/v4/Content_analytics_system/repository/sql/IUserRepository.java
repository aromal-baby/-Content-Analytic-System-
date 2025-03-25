package com.v4.Content_analytics_system.repository.sql;

import com.v4.Content_analytics_system.model.entity.sql.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Long> {


    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    // To check whether they already exists
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
