package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.InternshipApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternshipApplicationRepository extends JpaRepository<InternshipApplication, Long> {
    List<InternshipApplication> findByLearnerId(Long learnerId);
    List<InternshipApplication> findByInternshipId(Long internshipId);
    Optional<InternshipApplication> findByInternshipIdAndLearnerId(Long internshipId, Long learnerId);
}