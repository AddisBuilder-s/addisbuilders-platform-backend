package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.ApplyInternshipRequest;
import com.AddisBuilders.Platform.DTO.CreateInternshipListingRequest;
import com.AddisBuilders.Platform.DTO.InternshipApplicationResponse;
import com.AddisBuilders.Platform.DTO.InternshipListingResponse;
import com.AddisBuilders.Platform.Model.InternshipApplication;
import com.AddisBuilders.Platform.Model.InternshipListing;
import com.AddisBuilders.Platform.Model.Organization;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.ApplicationStatus;
import com.AddisBuilders.Platform.Model.enums.InternshipStatus;
import com.AddisBuilders.Platform.Repository.InternshipApplicationRepository;
import com.AddisBuilders.Platform.Repository.InternshipListingRepository;
import com.AddisBuilders.Platform.Repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service encapsulating core domain workflows for internship postings and student applications[cite: 1, 2].
 */
@Service
@RequiredArgsConstructor
public class InternshipService {

    private final InternshipListingRepository internshipRepository;
    private final InternshipApplicationRepository applicationRepository;
    private final OrganizationRepository organizationRepository;

    /**
     * Creates a new internship posting linked to an organization[cite: 1, 2].
     *
     * @param request     DTO payload with title, description, workMode, and deadline[cite: 1, 2].
     * @param currentUser Authenticated coordinator or employer[cite: 1, 2, 3].
     * @return InternshipListingResponse with persisted listing state.
     */
    @Transactional
    public InternshipListingResponse createListing(CreateInternshipListingRequest request, User currentUser) {
        // Validate target organization existence
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + request.organizationId()));

        // Construct entity with status PUBLISHED for active viewing[cite: 1, 2, 4]
        InternshipListing listing = InternshipListing.builder()
                .organization(organization) // Relates organization_id[cite: 1, 2]
                .coordinator(currentUser)  // Relates coordinator_id[cite: 1, 2]
                .title(request.title())
                .description(request.description())
                .requirements(request.requirements())
                .workMode(request.workMode()) // Enforces WorkMode ENUM (ONSITE, HYBRID, REMOTE)[cite: 3]
                .deadline(request.deadline())
                .status(InternshipStatus.PUBLISHED) // Default active status[cite: 4]
                .build();

        InternshipListing savedListing = internshipRepository.save(listing);
        return mapToListingResponse(savedListing);
    }

    /**
     * Retrieves all published internship opportunities for student discovery[cite: 1, 2, 4].
     */
    @Transactional(readOnly = true)
    public List<InternshipListingResponse> getPublishedListings() {
        return internshipRepository.findByStatus(InternshipStatus.PUBLISHED)
                .stream()
                .map(this::mapToListingResponse)
                .toList();
    }

    /**
     * Submits a student application for a specified internship listing[cite: 1, 2].
     * Enforces duplicate submission prevention using unique constraints[cite: 1, 2].
     *
     * @param internshipId Target listing ID[cite: 1, 2].
     * @param request      DTO containing cover letter and resume link[cite: 1, 2].
     * @param learner      Authenticated student user[cite: 1, 2, 3].
     * @return InternshipApplicationResponse confirming application receipt.
     */
    @Transactional
    public InternshipApplicationResponse applyForInternship(Long internshipId, ApplyInternshipRequest request, User learner) {
        // Retrieve target internship listing
        InternshipListing listing = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new IllegalArgumentException("Internship listing not found with ID: " + internshipId));

        // Enforce constraint: Students can apply at most once per internship listing[cite: 1, 2]
        boolean alreadyApplied = applicationRepository.findByInternshipIdAndLearnerId(internshipId, learner.getId()).isPresent();
        if (alreadyApplied) {
            throw new IllegalArgumentException("Conflict: You have already submitted an application for this internship.");
        }

        // Build application entity matching database columns[cite: 1, 2]
        InternshipApplication application = InternshipApplication.builder()
                .internship(listing)
                .learner(learner) // Relates learner_id[cite: 1, 2]
                .coverLetter(request.coverLetter())
                .resumeUrl(request.resumeUri())
                .status(ApplicationStatus.SUBMITTED) // Initial ApplicationStatus ENUM[cite: 4]
                .build();

        InternshipApplication savedApp = applicationRepository.save(application);
        return mapToAppResponse(savedApp);
    }

    /**
     * Fetches all internship applications submitted by the logged-in student[cite: 1, 2].
     */
    @Transactional(readOnly = true)
    public List<InternshipApplicationResponse> getApplicationsForLearner(User learner) {
        return applicationRepository.findByLearnerId(learner.getId())
                .stream()
                .map(this::mapToAppResponse)
                .toList();
    }

    // Helper mapper for Internship Listings[cite: 1, 2]
    private InternshipListingResponse mapToListingResponse(InternshipListing listing) {
        return InternshipListingResponse.builder()
                .id(listing.getId())
                .organizationId(listing.getOrganization().getId())
                .organizationName(listing.getOrganization().getName())
                .title(listing.getTitle())
                .description(listing.getDescription())
                .requirements(listing.getRequirements())
                .workMode(listing.getWorkMode())
                .deadline(listing.getDeadline())
                .status(listing.getStatus())
                .createdAt(listing.getCreatedAt())
                .build();
    }

    // Helper mapper for Internship Applications[cite: 1, 2]
    private InternshipApplicationResponse mapToAppResponse(InternshipApplication app) {
        return InternshipApplicationResponse.builder()
                .id(app.getId())
                .internshipId(app.getInternship().getId())
                .internshipTitle(app.getInternship().getTitle())
                .learnerId(app.getLearner().getId())
                .learnerName(app.getLearner().getFullName())
                .coverLetter(app.getCoverLetter())
                .resumeUri(app.getResumeUrl())
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .build();
    }
}