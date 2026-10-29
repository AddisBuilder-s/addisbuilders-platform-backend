package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignmentId(Long assignmentId);
    List<Submission> findByLearnerId(Long learnerId);
    Optional<Submission> findByAssignmentIdAndLearnerId(Long assignmentId, Long learnerId);
}