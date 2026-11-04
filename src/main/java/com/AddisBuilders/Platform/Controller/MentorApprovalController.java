package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.CoordinatorDecisionRequest;
import com.AddisBuilders.Platform.DTO.MentorRequestResponse;
import com.AddisBuilders.Platform.DTO.MentorStatusResponse;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.MentorApprovalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MentorApprovalController {
    private final MentorApprovalService service;

    @GetMapping("/mentors/me/status")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<MentorStatusResponse> getStatus(@AuthenticationPrincipal User mentor) {
        return ResponseEntity.ok(service.status(mentor));
    }

    @GetMapping("/admin/mentor-requests")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<MentorRequestResponse>> listPending() {
        return ResponseEntity.ok(service.listPending());
    }

    @PostMapping("/admin/mentor-requests/{id}/decision")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<MentorRequestResponse> decide(
            @PathVariable Long id, @Valid @RequestBody CoordinatorDecisionRequest request,
            @AuthenticationPrincipal User actor, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(service.decide(id, request.status(), request.notes(), actor, servletRequest));
    }
}
