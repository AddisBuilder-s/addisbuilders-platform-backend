package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.AssignmentResponse;
import com.AddisBuilders.Platform.DTO.CreateAssignmentRequest;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing course assignments.
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    /**
     * Create an assignment for a course. Restricted to MENTOR and SUPER_ADMIN roles.
     */
    @PostMapping("/{courseId}/assignments")
    @PreAuthorize("hasAnyRole('MENTOR', 'SUPER_ADMIN')")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateAssignmentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(assignmentService.createAssignment(courseId, request, currentUser));
    }

    /**
     * Retrieve all assignments for a given course. Accessible by students and mentors.
     */
    @GetMapping("/{courseId}/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByCourse(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId));
    }
}
