package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepostiory extends JpaRepository<Organization,Long> {
    // Custom query to find an organization profile by its owning user ID
    Optional<Organization> findByUserId(Long userId);

    // Check if an organization name already exists to prevent duplicates
    boolean existsByName(String name);
}
