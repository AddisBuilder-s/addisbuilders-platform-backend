package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.InternshipApplicationResponse;
import com.AddisBuilders.Platform.DTO.UpdateApplicationStatusRequest;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Service.InternshipService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final InternshipService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('LEARNER', 'EMPLOYER', 'SUPER_ADMIN') or @accountAuthorization.isApprovedCoordinator(authentication)")
    public ResponseEntity<List<InternshipApplicationResponse>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long internshipId,
            @RequestParam(required = false) Long applicantId) {
        List<InternshipApplicationResponse> result = user.getRole() == UserRole.LEARNER
                ? service.getApplicationsForLearner(user)
                : service.getApplicationsForManager(user);
        if (internshipId != null) result = result.stream().filter(app -> app.internshipId().equals(internshipId)).toList();
        if (applicantId != null && user.getRole() == UserRole.SUPER_ADMIN) {
            result = result.stream().filter(app -> app.learnerId().equals(applicantId)).toList();
        }
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'SUPER_ADMIN') or @accountAuthorization.isApprovedCoordinator(authentication)")
    public ResponseEntity<InternshipApplicationResponse> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateApplicationStatusRequest request,
            @AuthenticationPrincipal User actor, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(service.updateApplicationStatus(id, request.status(), request.notes(), actor, servletRequest));
    }
}
