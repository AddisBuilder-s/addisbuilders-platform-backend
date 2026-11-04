package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.CreateOrganizationRequest;
import com.AddisBuilders.Platform.DTO.OrganizationResponse;
import com.AddisBuilders.Platform.DTO.OrganizationVerificationRequest;
import com.AddisBuilders.Platform.DTO.UpdateOrganizationRequest;
import com.AddisBuilders.Platform.DTO.VerificationDecisionRequest;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.OrganizationService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller providing endpoints for partner organization management[cite: 1, 2].
 */
@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * Endpoint to create an organization profile[cite: 1, 2].
     * Restricted to EMPLOYER, INTERNSHIP_COORDINATOR, and SUPER_ADMIN roles[cite: 3].
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'SUPER_ADMIN') or @accountAuthorization.isApprovedCoordinator(authentication)")
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.status(201).body(organizationService.createOrganization(request, currentUser));
    }

    /**
     * Public endpoint to retrieve all registered partner organizations[cite: 1, 2].
     */
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    @GetMapping({"/my", "/me"})
    @PreAuthorize("hasRole('EMPLOYER') or @accountAuthorization.isApprovedCoordinator(authentication)")
    public ResponseEntity<OrganizationResponse> getMyOrganization(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(organizationService.getMyOrganization(currentUser));
    }

    @PatchMapping({"/my", "/me"})
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<OrganizationResponse> updateMyOrganization(
            @Valid @RequestBody UpdateOrganizationRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(organizationService.updateMyOrganization(currentUser, request));
    }

    @PostMapping("/me/verification")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<OrganizationResponse> submitVerification(
            @Valid @RequestBody OrganizationVerificationRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(organizationService.submitVerification(currentUser, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable Long id) {
        return ResponseEntity.ok(organizationService.getOrganizationById(id));
    }
}
