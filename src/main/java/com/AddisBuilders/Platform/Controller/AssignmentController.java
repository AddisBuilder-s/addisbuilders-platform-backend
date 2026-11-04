package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.AssignmentResponse;
import com.AddisBuilders.Platform.DTO.CreateAssignmentRequest;
import com.AddisBuilders.Platform.DTO.UpdateAssignmentRequest;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
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
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<AssignmentResponse> createAssignment(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateAssignmentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.createAssignment(courseId, request, currentUser));
    }

    // Get assignments for a specific course
    @GetMapping("/courses/{courseId}/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAssignmentsByCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId, currentUser));
    }

    // Global assignments endpoint matching Dina's assignmentsService.getAssignments()
    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAllAssignments(
            @RequestParam(required = false) Long courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        if (courseId != null) {
            return ResponseEntity.ok(assignmentService.getAssignmentsByCourse(courseId, currentUser));
        }
        return ResponseEntity.ok(assignmentService.getAllAssignments(currentUser));
    }

    // Retrieve one assignment
    @GetMapping("/assignments/{id}")
    public ResponseEntity<AssignmentResponse> getAssignment(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(assignmentService.getAssignment(id, currentUser));
    }

    // Update an assignment (owner mentor or super admin)
    @PatchMapping("/assignments/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, request, currentUser));
    }

    // Delete an assignment (owner mentor or super admin)
    @DeleteMapping("/assignments/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        assignmentService.deleteAssignment(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
