package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Custom query method to look up users by email during authentication
    Optional<User> findByEmail(String email);

    // Custom query method to check if an email is already registered
    boolean existsByEmail(String email);

    List<User> findByRoleAndCoordinatorStatusOrderByCreatedAtDesc(UserRole role, ApprovalStatus status);

    List<User> findByRoleAndMentorStatusOrderByCreatedAtDesc(UserRole role, ApprovalStatus status);

    List<User> findByRole(UserRole role);
}
