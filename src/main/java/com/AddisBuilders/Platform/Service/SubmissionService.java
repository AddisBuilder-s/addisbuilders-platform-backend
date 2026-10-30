package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.GradeSubmissionRequest;
import com.AddisBuilders.Platform.DTO.SubmissionResponse;
import com.AddisBuilders.Platform.DTO.SubmitAssignmentRequest;
import com.AddisBuilders.Platform.Model.*;
import com.AddisBuilders.Platform.Model.enums.EnrollmentStatus;
import com.AddisBuilders.Platform.Model.enums.SubmissionStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Repository.AssignmentRepository;
import com.AddisBuilders.Platform.Repository.EnrollmentRepository;
import com.AddisBuilders.Platform.Repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service managing student assignment submissions and mentor grading workflows.
 */
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Allows a student to submit or update their work for an assignment.
     */
    @Transactional
    public SubmissionResponse submitAssignment(Long assignmentId, SubmitAssignmentRequest request, User learner) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + assignmentId));

        Course course = assignment.getCourse();
        if (!enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                learner.getId(), course.getId(), EnrollmentStatus.ACTIVE)) {
            throw new AccessDeniedException("You must be enrolled in this course to submit work");
        }

        if (assignment.getDueAt() != null && LocalDateTime.now().isAfter(assignment.getDueAt())) {
            throw new IllegalArgumentException("The deadline for this assignment has passed");
        }

        // Check if submission already exists for this learner & assignment (supports resubmissions)
        Submission submission = submissionRepository.findByAssignmentIdAndLearnerId(assignmentId, learner.getId())
                .orElse(Submission.builder()
                        .assignment(assignment)
                        .learner(learner)
                        .build());

        submission.setTextAnswer(request.textAnswer());
        submission.setFileUrl(request.fileUrl());
        submission.setStatus(SubmissionStatus.SUBMITTED);

        // If it's a new submission, submittedAt is automatically set by @CreationTimestamp.
        // For updates, we can refresh submittedAt or keep original.

        Submission savedSubmission = submissionRepository.save(submission);
        return mapToResponse(savedSubmission);
    }

    /**
     * Allows a mentor or super admin to grade a student submission.
     */
    @Transactional
    public SubmissionResponse gradeSubmission(Long submissionId, GradeSubmissionRequest request, User currentUser) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found with ID: " + submissionId));

        Course course = submission.getAssignment().getCourse();
        if (!course.getMentor().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("You do not own the course for this submission");
        }

        Integer maxPoints = submission.getAssignment().getPoints();
        if (maxPoints != null && request.grade().compareTo(BigDecimal.valueOf(maxPoints)) > 0) {
            throw new IllegalArgumentException("Grade cannot exceed the maximum points for this assignment (" + maxPoints + ")");
        }

        submission.setGrade(request.grade());
        submission.setFeedback(request.feedback());
        submission.setStatus(request.status());
        submission.setGradedAt(LocalDateTime.now());

        Submission updatedSubmission = submissionRepository.save(submission);
        return mapToResponse(updatedSubmission);
    }

    /**
     * Retrieves all submissions for a given assignment (for mentors/instructors).
     */
    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsByAssignment(Long assignmentId, User currentUser) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found with ID: " + assignmentId));

        Course course = assignment.getCourse();
        if (!course.getMentor().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("You do not own this course");
        }

        return submissionRepository.findByAssignmentId(assignmentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getAllInstructorSubmissions(User currentUser) {
        // If mentor, filter submissions for assignments belonging to their courses
        // If SUPER_ADMIN, return all submissions in the platform
        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            return submissionRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        return submissionRepository.findByAssignmentCourseMentorId(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Helper mapper to convert entity to response DTO.
     */
    private SubmissionResponse mapToResponse(Submission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment().getId())
                .assignmentTitle(submission.getAssignment().getTitle())
                .learnerId(submission.getLearner().getId())
                .learnerName(submission.getLearner().getFullName())
                .textAnswer(submission.getTextAnswer())
                .fileUrl(submission.getFileUrl())
                .status(submission.getStatus())
                .grade(submission.getGrade())
                .feedback(submission.getFeedback())
                .submittedAt(submission.getSubmittedAt())
                .gradedAt(submission.getGradedAt())
                .build();
    }
}