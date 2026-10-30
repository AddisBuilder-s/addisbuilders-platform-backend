package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.*;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.InternshipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller exposing endpoints for internship postings and application processing[cite: 1, 2].
 */
@RestController
@RequestMapping("/internships")
@RequiredArgsConstructor
public class InternshipController {

    private final InternshipService internshipService;

    /**
     * Endpoint to publish an internship opportunity[cite: 1, 2].
     * Restricted to EMPLOYER, INTERNSHIP_COORDINATOR, and SUPER_ADMIN roles[cite: 3].
     */
    @PostMapping("/listings")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'INTERNSHIP_COORDINATOR', 'SUPER_ADMIN')")
    public ResponseEntity<InternshipListingResponse> createListing(
            @Valid @RequestBody CreateInternshipListingRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(internshipService.createListing(request, currentUser));
    }

    /**
     * Public endpoint to browse published internship listings[cite: 1, 2, 4].
     */
    @GetMapping("/listings")
    public ResponseEntity<List<InternshipListingResponse>> getPublishedListings() {
        return ResponseEntity.ok(internshipService.getPublishedListings());
    }

    /**
     * Endpoint for students to submit an application for an internship[cite: 1, 2].
     * Restricted strictly to users with the LEARNER role[cite: 3].
     */
    @PostMapping("/listings/{internshipId}/apply")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<InternshipApplicationResponse> applyForInternship(
            @PathVariable Long internshipId,
            @Valid @RequestBody ApplyInternshipRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(internshipService.applyForInternship(internshipId, request, currentUser));
    }

    /**
     * Endpoint for students to view all their submitted internship applications[cite: 1, 2].
     * Restricted strictly to the LEARNER role[cite: 3].
     */
    @GetMapping("/applications/my")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<List<InternshipApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(internshipService.getApplicationsForLearner(currentUser));
    }
}