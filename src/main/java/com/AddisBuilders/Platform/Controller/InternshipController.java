package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.*;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Service.InternshipService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/internships")
@RequiredArgsConstructor
public class InternshipController {
    private final InternshipService internshipService;

    @PostMapping("/listings")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'SUPER_ADMIN') or @accountAuthorization.isApprovedCoordinator(authentication)")
    public ResponseEntity<InternshipListingResponse> createListing(
            @Valid @RequestBody CreateInternshipListingRequest request,
            @AuthenticationPrincipal User currentUser, HttpServletRequest servletRequest) {
        return ResponseEntity.status(201).body(internshipService.createListing(request, currentUser, servletRequest));
    }

    @GetMapping({"", "/listings"})
    public ResponseEntity<List<InternshipListingResponse>> getPublishedListings(
            @AuthenticationPrincipal User viewer,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String workMode,
            @RequestParam(required = false) String search) {
        List<InternshipListingResponse> result = internshipService.getPublishedListings(viewer).stream()
                .filter(item -> category == null || category.isBlank() || "all".equalsIgnoreCase(category)
                        || (item.category() != null && item.category().equalsIgnoreCase(category)))
                .filter(item -> workMode == null || workMode.isBlank() || "all".equalsIgnoreCase(workMode)
                        || item.workMode().name().equalsIgnoreCase(workMode))
                .filter(item -> search == null || search.isBlank()
                        || (item.title() + " " + item.organizationName() + " " + item.description())
                        .toLowerCase(Locale.ROOT).contains(search.trim().toLowerCase(Locale.ROOT)))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping({"/listings/{id}", "/{id}"})
    public ResponseEntity<InternshipListingResponse> getListing(
            @PathVariable Long id, @AuthenticationPrincipal User viewer) {
        return ResponseEntity.ok(internshipService.getListing(id, viewer));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'SUPER_ADMIN') or @accountAuthorization.isApprovedCoordinator(authentication)")
    public ResponseEntity<List<InternshipListingResponse>> getMine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(internshipService.getMyListings(user));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<InternshipListingResponse>> getAllForAdmin(@AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(internshipService.getAllForAdmin(admin));
    }

    @PatchMapping({"/listings/{id}", "/{id}"})
    @PreAuthorize("hasAnyRole('EMPLOYER', 'SUPER_ADMIN') or @accountAuthorization.isApprovedCoordinator(authentication)")
    public ResponseEntity<InternshipListingResponse> update(
            @PathVariable Long id, @Valid @RequestBody UpdateInternshipListingRequest request,
            @AuthenticationPrincipal User actor, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(internshipService.updateListing(id, request, actor, servletRequest));
    }

    @DeleteMapping({"/listings/{id}", "/{id}"})
    @PreAuthorize("hasAnyRole('EMPLOYER', 'SUPER_ADMIN') or @accountAuthorization.isApprovedCoordinator(authentication)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id, @AuthenticationPrincipal User actor, HttpServletRequest servletRequest) {
        internshipService.deleteListing(id, actor, servletRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping({"/listings/{id}/close", "/{id}/close"})
    @PreAuthorize("hasAnyRole('EMPLOYER', 'SUPER_ADMIN') or @accountAuthorization.isApprovedCoordinator(authentication)")
    public ResponseEntity<InternshipListingResponse> close(
            @PathVariable Long id, @AuthenticationPrincipal User actor, HttpServletRequest servletRequest) {
        internshipService.closeListing(id, actor, servletRequest);
        return ResponseEntity.ok(internshipService.getListing(id, actor));
    }

    @PostMapping({"/listings/{id}/save", "/{id}/save"})
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<java.util.Map<String, Boolean>> toggleSave(
            @PathVariable Long id, @AuthenticationPrincipal User learner) {
        return ResponseEntity.ok(java.util.Map.of("isSaved", internshipService.toggleSaved(id, learner)));
    }

    @GetMapping("/saved")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<List<InternshipListingResponse>> getSaved(@AuthenticationPrincipal User learner) {
        return ResponseEntity.ok(internshipService.getSaved(learner));
    }

    @PostMapping({"/listings/{internshipId}/apply", "/{internshipId}/applications"})
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<InternshipApplicationResponse> apply(
            @PathVariable Long internshipId, @Valid @RequestBody ApplyInternshipRequest request,
            @AuthenticationPrincipal User learner) {
        return ResponseEntity.status(201).body(internshipService.applyForInternship(internshipId, request, learner));
    }

    @GetMapping("/applications/my")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<List<InternshipApplicationResponse>> getMyApplications(@AuthenticationPrincipal User learner) {
        return ResponseEntity.ok(internshipService.getApplicationsForLearner(learner));
    }
}
