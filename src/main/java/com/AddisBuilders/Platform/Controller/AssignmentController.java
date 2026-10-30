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

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    // Create assignment under a course
    @PostMapping("/courses/{courseId}/assignments")
    @PreAuthorize("hasAnyRole('MENTOR', 'SUPER_ADMIN')")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateAssignmentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(assignmentService.createAssignment(courseId, request, currentUser));
    }

    // Get assignments for a specific course
    @GetMapping("/courses/{courseId}/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByCourse(
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId));
    }

    // Global assignments endpoint matching Dina's assignmentsService.getAssignments()
    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAllAssignments(
            @RequestParam(required = false) Long courseId
    ) {
        if (courseId != null) {
            return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId));
        }
        return ResponseEntity.ok(assignmentService.getAllAssignments());
    }
}