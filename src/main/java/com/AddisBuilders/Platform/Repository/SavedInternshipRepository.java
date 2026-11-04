package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.SavedInternship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedInternshipRepository extends JpaRepository<SavedInternship, Long> {
    Optional<SavedInternship> findByUserIdAndInternshipId(Long userId, Long internshipId);
    List<SavedInternship> findByUserIdOrderByCreatedAtDesc(Long userId);
}
