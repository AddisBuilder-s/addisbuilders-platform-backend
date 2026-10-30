package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.EnrollmentResponse;
import com.AddisBuilders.Platform.DTO.UpdateEnrollmentStatusRequest;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // Matches Dina's apiClient.get('/users/me/enrollments')
    @GetMapping("/users/me/enrollments")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(enrollmentService.getStudentEnrollments(currentUser.getId()));
    }

    // Matches Dina's apiClient.get('/instructor/students')
    @GetMapping("/instructor/students")
    @PreAuthorize("hasAnyRole('MENTOR', 'SUPER_ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getInstructorStudents(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(enrollmentService.getInstructorStudents(currentUser.getId()));
    }

    // Matches Dina's apiClient.patch('/enrollments/{id}/status')
    @PatchMapping("/enrollments/{id}/status")
    @PreAuthorize("hasAnyRole('MENTOR', 'SUPER_ADMIN')")
    public ResponseEntity<EnrollmentResponse> updateEnrollmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEnrollmentStatusRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(enrollmentService.updateEnrollmentStatus(id, request.status(), currentUser));
    }
}