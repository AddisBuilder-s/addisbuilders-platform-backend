package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.CoordinatorDecisionRequest;
import com.AddisBuilders.Platform.DTO.CoordinatorRequestResponse;
import com.AddisBuilders.Platform.DTO.CoordinatorStatusResponse;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.CoordinatorService;
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
public class CoordinatorController {
    private final CoordinatorService service;

    @GetMapping("/coordinators/me/status")
    @PreAuthorize("hasRole('INTERNSHIP_COORDINATOR')")
    public ResponseEntity<CoordinatorStatusResponse> getStatus(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.status(user));
    }

    @GetMapping("/admin/coordinator-requests")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<CoordinatorRequestResponse>> listPending() {
        return ResponseEntity.ok(service.listPending());
    }

    @PostMapping("/admin/coordinator-requests/{id}/decision")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CoordinatorRequestResponse> decide(
            @PathVariable Long id, @Valid @RequestBody CoordinatorDecisionRequest request,
            @AuthenticationPrincipal User actor, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(service.decide(id, request.status(), request.notes(), actor, servletRequest));
    }
}
