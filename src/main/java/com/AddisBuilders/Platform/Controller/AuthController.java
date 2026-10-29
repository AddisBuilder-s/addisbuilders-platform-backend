package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.*;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.AuthService;
import com.AddisBuilders.Platform.Service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    //Get currently authenticated user details using their JWT token
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        AuthResponse response = AuthResponse.builder()
                .token(null) // No need to re-issue token here
                .type("Bearer")
                .email(currentUser.getEmail())
                .fullName(currentUser.getFullName())
                .role(currentUser.getRole())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Controller providing endpoints for partner organization management[cite: 1, 2].
     */
    @RestController
    @RequestMapping("/api/organizations")
    @RequiredArgsConstructor
    public static class OrganizationController {

        private final OrganizationService organizationService;

        /**
         * Endpoint to create an organization profile[cite: 1, 2].
         * Restricted to EMPLOYER, INTERNSHIP_COORDINATOR, and SUPER_ADMIN roles[cite: 3].
         */
        @PostMapping
        @PreAuthorize("hasAnyRole('EMPLOYER', 'INTERNSHIP_COORDINATOR', 'SUPER_ADMIN')")
        public ResponseEntity<OrganizationResponse> createOrganization(
                @Valid @RequestBody CreateOrganizationRequest request,
                @AuthenticationPrincipal User currentUser
        ) {
            return ResponseEntity.ok(organizationService.createOrganization(request, currentUser));
        }

        /**
         * Public endpoint to retrieve all registered partner organizations[cite: 1, 2].
         */
        @GetMapping
        public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
            return ResponseEntity.ok(organizationService.getAllOrganizations());
        }
    }
}