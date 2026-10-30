package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.GradeSubmissionRequest;
import com.AddisBuilders.Platform.DTO.SubmissionResponse;
import com.AddisBuilders.Platform.DTO.SubmitAssignmentRequest;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller handling student submissions and mentor grading endpoints.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * Submit an assignment. Restricted to LEARNER role.
     */
    @PostMapping("/assignments/{assignmentId}/submit")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<SubmissionResponse> submitAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody SubmitAssignmentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(submissionService.submitAssignment(assignmentId, request, currentUser));
    }

    /**
     * Grade a student submission. Restricted to MENTOR and SUPER_ADMIN roles.
     */
    @PatchMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasAnyRole('MENTOR', 'SUPER_ADMIN')")
    public ResponseEntity<SubmissionResponse> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeSubmissionRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(submissionService.gradeSubmission(submissionId, request, currentUser));
    }

    /**
     * Get all submissions for an assignment. Restricted to course mentors and admins.
     */
    @GetMapping("/assignments/{assignmentId}/submissions")
    @PreAuthorize("hasAnyRole('MENTOR', 'SUPER_ADMIN')")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsByAssignment(
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(submissionService.getSubmissionsByAssignment(assignmentId, currentUser));
    }

    // Get submissions by assignment OR globally for all mentor assignments
    @GetMapping({"/assignments/{assignmentId}/submissions", "/assignments/submissions"})
    @PreAuthorize("hasAnyRole('MENTOR', 'SUPER_ADMIN')")
    public ResponseEntity<List<SubmissionResponse>> getSubmissions(
            @PathVariable(required = false) Long assignmentId,
            @RequestParam(required = false) Long studentId,
            @AuthenticationPrincipal User currentUser
    ) {
        if (assignmentId != null) {
            return ResponseEntity.ok(submissionService.getSubmissionsByAssignment(assignmentId, currentUser));
        }
        return ResponseEntity.ok(submissionService.getAllInstructorSubmissions(currentUser));
    }
}