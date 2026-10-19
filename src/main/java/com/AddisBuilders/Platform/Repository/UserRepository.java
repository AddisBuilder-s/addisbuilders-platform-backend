package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Custom query method to look up users by email during authentication
    Optional<User> findByEmail(String email);

    // Custom query method to check if an email is already registered
    boolean existsByEmail(String email);
}