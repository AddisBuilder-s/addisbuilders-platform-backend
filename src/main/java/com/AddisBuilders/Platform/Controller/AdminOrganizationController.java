package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.OrganizationResponse;
import com.AddisBuilders.Platform.DTO.VerificationDecisionRequest;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.OrganizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/organizations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminOrganizationController {
    private final OrganizationService service;

    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> list() {
        return ResponseEntity.ok(service.getAllOrganizationsAdmin());
    }

    @PostMapping("/{id}/verification-decision")
    public ResponseEntity<OrganizationResponse> decide(
            @PathVariable Long id,
            @Valid @RequestBody VerificationDecisionRequest request,
            @AuthenticationPrincipal User admin,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(service.decideVerification(id, request, admin, servletRequest));
    }
}
