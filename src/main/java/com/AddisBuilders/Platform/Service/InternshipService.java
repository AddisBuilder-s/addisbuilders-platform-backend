package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.Exception.ResourceNotFoundException;

import com.AddisBuilders.Platform.DTO.*;
import com.AddisBuilders.Platform.Model.*;
import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;
import com.AddisBuilders.Platform.Model.enums.ApplicationStatus;
import com.AddisBuilders.Platform.Model.enums.InternshipStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Model.enums.VerificationStatus;
import com.AddisBuilders.Platform.Repository.InternshipApplicationRepository;
import com.AddisBuilders.Platform.Repository.InternshipListingRepository;
import com.AddisBuilders.Platform.Repository.OrganizationRepository;
import com.AddisBuilders.Platform.Repository.SavedInternshipRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InternshipService {
    private final InternshipListingRepository internshipRepository;
    private final InternshipApplicationRepository applicationRepository;
    private final OrganizationRepository organizationRepository;
    private final SavedInternshipRepository savedInternships;
    private final NotificationService notifications;
    private final AuditLogService auditLog;

    @Transactional
    public InternshipListingResponse createListing(CreateInternshipListingRequest request, User actor,
                                                   HttpServletRequest servletRequest) {
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        requireApprovedCoordinator(actor);
        if (actor.getRole() == UserRole.EMPLOYER) {
            if (!organization.getOwner().getId().equals(actor.getId())) {
                throw new AccessDeniedException("You can only publish for your own organization");
            }
            if (request.status() != InternshipStatus.DRAFT
                    && organization.getVerificationStatus() != VerificationStatus.APPROVED) {
                throw new AccessDeniedException("Organization verification must be approved before publishing listings");
            }
        }
        InternshipListing listing = InternshipListing.builder()
                .organization(organization)
                .coordinator(actor.getRole() == UserRole.INTERNSHIP_COORDINATOR ? actor : null)
                .title(request.title().trim())
                .description(request.description())
                .requirements(request.requirements())
                .workMode(request.workMode())
                .location(request.location())
                .category(request.category())
                .responsibilities(request.responsibilities())
                .eligibility(request.eligibility())
                .stipend(request.stipend())
                .durationMonths(request.durationMonths() == null ? 6 : request.durationMonths())
                .deadline(request.deadline())
                .status(request.status() == null ? InternshipStatus.PUBLISHED : request.status())
                .build();
        InternshipListing saved = internshipRepository.save(listing);
        auditLog.record(actor, "INTERNSHIP_CREATED", "INTERNSHIP", saved.getId(), saved.getTitle(), servletRequest);
        return mapToListingResponse(saved, actor);
    }

    @Transactional(readOnly = true)
    public List<InternshipListingResponse> getPublishedListings(User viewer) {
        return internshipRepository.findByStatus(InternshipStatus.PUBLISHED).stream()
                .filter(this::isPubliclyAvailable)
                .map(listing -> mapToListingResponse(listing, viewer)).toList();
    }

    @Transactional(readOnly = true)
    public List<InternshipListingResponse> getMyListings(User user) {
        requireApprovedCoordinator(user);
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return internshipRepository.findAll().stream().map(listing -> mapToListingResponse(listing, user)).toList();
        }
        List<InternshipListing> listings = user.getRole() == UserRole.EMPLOYER
                ? internshipRepository.findByOrganizationOwnerIdOrderByCreatedAtDesc(user.getId())
                : internshipRepository.findByCoordinatorIdOrderByCreatedAtDesc(user.getId());
        return listings.stream().map(listing -> mapToListingResponse(listing, user)).toList();
    }

    @Transactional(readOnly = true)
    public List<InternshipListingResponse> getAllForAdmin(User admin) {
        if (admin == null || admin.getRole() != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Administrator access is required");
        }
        return internshipRepository.findAll().stream().map(listing -> mapToListingResponse(listing, admin)).toList();
    }

    @Transactional(readOnly = true)
    public InternshipListingResponse getListing(Long id, User viewer) {
        InternshipListing listing = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Internship listing not found"));
        if (listing.getStatus() == InternshipStatus.PUBLISHED && listing.getDeadline().isBefore(LocalDateTime.now())
                && !ownsListing(listing, viewer)) {
            throw new AccessDeniedException("Internship listing is no longer accepting applications");
        }
        if (!isPubliclyAvailable(listing) && !ownsListing(listing, viewer)) {
            throw new AccessDeniedException("Internship listing is not public");
        }
        return mapToListingResponse(listing, viewer);
    }

    @Transactional
    public InternshipListingResponse updateListing(Long id, UpdateInternshipListingRequest request,
                                                   User actor, HttpServletRequest servletRequest) {
        InternshipListing listing = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Internship listing not found"));
        assertCanManage(listing, actor);
        requireApprovedCoordinator(actor);
        if (request.title() != null) listing.setTitle(request.title().trim());
        if (request.description() != null) listing.setDescription(request.description());
        if (request.requirements() != null) listing.setRequirements(request.requirements());
        if (request.workMode() != null) listing.setWorkMode(request.workMode());
        if (request.location() != null) listing.setLocation(request.location().trim());
        if (request.category() != null) listing.setCategory(request.category().trim());
        if (request.responsibilities() != null) listing.setResponsibilities(request.responsibilities());
        if (request.eligibility() != null) listing.setEligibility(request.eligibility());
        if (request.stipend() != null) listing.setStipend(request.stipend());
        if (request.durationMonths() != null) listing.setDurationMonths(request.durationMonths());
        if (request.deadline() != null) listing.setDeadline(request.deadline());
        if (request.status() != null) {
            if (request.status() == InternshipStatus.PUBLISHED && actor.getRole() == UserRole.EMPLOYER
                    && listing.getOrganization().getVerificationStatus() != VerificationStatus.APPROVED) {
                throw new AccessDeniedException("Organization verification must be approved before publishing listings");
            }
            listing.setStatus(request.status());
        }
        InternshipListing saved = internshipRepository.save(listing);
        auditLog.record(actor, "INTERNSHIP_UPDATED", "INTERNSHIP", id, "Listing updated", servletRequest);
        return mapToListingResponse(saved, actor);
    }

    @Transactional
    public boolean toggleSaved(Long internshipId, User learner) {
        InternshipListing listing = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship listing not found"));
        if (!isPubliclyAvailable(listing)) {
            throw new IllegalArgumentException("Only published internships can be saved");
        }
        var existing = savedInternships.findByUserIdAndInternshipId(learner.getId(), internshipId);
        if (existing.isPresent()) {
            savedInternships.delete(existing.get());
            return false;
        }
        savedInternships.save(SavedInternship.builder().user(learner).internship(listing).build());
        return true;
    }

    @Transactional(readOnly = true)
    public List<InternshipListingResponse> getSaved(User learner) {
        return savedInternships.findByUserIdOrderByCreatedAtDesc(learner.getId()).stream()
                .filter(saved -> isPubliclyAvailable(saved.getInternship()))
                .map(saved -> mapToListingResponse(saved.getInternship(), learner)).toList();
    }

    @Transactional
    public InternshipApplicationResponse applyForInternship(Long internshipId, ApplyInternshipRequest request, User learner) {
        InternshipListing listing = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Internship listing not found"));
        if (!isPubliclyAvailable(listing)) {
            throw new IllegalArgumentException("This internship is no longer accepting applications");
        }
        if (applicationRepository.findByInternshipIdAndLearnerId(internshipId, learner.getId()).isPresent()) {
            throw new com.AddisBuilders.Platform.Exception.ConflictException("You have already submitted an application for this internship");
        }
        InternshipApplication application = InternshipApplication.builder()
                .internship(listing).learner(learner).coverLetter(request.coverLetter())
                .resumeUrl(request.resumeUri()).status(ApplicationStatus.SUBMITTED).build();
        InternshipApplication saved = applicationRepository.save(application);
        auditLog.record(learner, "INTERNSHIP_APPLIED", "APPLICATION", saved.getId(), listing.getTitle(), null);
        notifications.notify(listing.getOrganization().getOwner(), "New internship application",
                learner.getFullName() + " applied for " + listing.getTitle(), "INFO", "/employer/applicants");
        return mapToAppResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InternshipApplicationResponse> getApplicationsForLearner(User learner) {
        return applicationRepository.findByLearnerId(learner.getId()).stream().map(this::mapToAppResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InternshipApplicationResponse> getApplicationsForManager(User manager) {
        requireApprovedCoordinator(manager);
        List<InternshipApplication> applications = manager.getRole() == UserRole.EMPLOYER
                ? applicationRepository.findByInternshipOrganizationOwnerIdOrderByAppliedAtDesc(manager.getId())
                : applicationRepository.findAllByOrderByAppliedAtDesc();
        return applications.stream().map(this::mapToAppResponse).toList();
    }

    @Transactional
    public InternshipApplicationResponse updateApplicationStatus(Long id, ApplicationStatus status, String notes,
                                                                 User actor, HttpServletRequest servletRequest) {
        InternshipApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        InternshipListing listing = application.getInternship();
        assertCanManage(listing, actor);
        requireApprovedCoordinator(actor);
        application.setStatus(status);
        application.setNotes(notes);
        InternshipApplication saved = applicationRepository.save(application);
        notifications.notify(application.getLearner(), "Application status updated",
                "Your application for " + listing.getTitle() + " is now " + status + ".",
                status == ApplicationStatus.ACCEPTED ? "SUCCESS" : "INFO", "/learner/applications");
        auditLog.record(actor, "APPLICATION_STATUS_UPDATED", "APPLICATION", id,
                "Status set to " + status, servletRequest);
        return mapToAppResponse(saved);
    }

    @Transactional
    public void closeListing(Long id, User actor, HttpServletRequest servletRequest) {
        InternshipListing listing = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Internship listing not found"));
        assertCanManage(listing, actor);
        requireApprovedCoordinator(actor);
        if (listing.getStatus() == InternshipStatus.CLOSED) {
            return;
        }
        listing.setStatus(InternshipStatus.CLOSED);
        internshipRepository.save(listing);
        auditLog.record(actor, "INTERNSHIP_CLOSED", "INTERNSHIP", id, listing.getTitle(), servletRequest);
    }

    @Transactional
    public void deleteListing(Long id, User actor, HttpServletRequest servletRequest) {
        InternshipListing listing = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Internship listing not found"));
        assertCanManage(listing, actor);
        requireApprovedCoordinator(actor);
        auditLog.record(actor, "INTERNSHIP_DELETED", "INTERNSHIP", id, listing.getTitle(), servletRequest);
        internshipRepository.delete(listing);
    }

    private InternshipListingResponse mapToListingResponse(InternshipListing listing, User viewer) {
        boolean saved = viewer != null && savedInternships.findByUserIdAndInternshipId(viewer.getId(), listing.getId()).isPresent();
        boolean applied = viewer != null && applicationRepository.findByInternshipIdAndLearnerId(listing.getId(), viewer.getId()).isPresent();
        User postedBy = listing.getCoordinator() != null ? listing.getCoordinator() : listing.getOrganization().getOwner();
        return InternshipListingResponse.builder().id(listing.getId())
                .organizationId(listing.getOrganization().getId()).organizationName(listing.getOrganization().getName())
                .title(listing.getTitle()).description(listing.getDescription()).requirements(listing.getRequirements())
                .workMode(listing.getWorkMode()).location(listing.getLocation()).category(listing.getCategory())
                .responsibilities(listing.getResponsibilities()).eligibility(listing.getEligibility())
                .stipend(listing.getStipend()).durationMonths(listing.getDurationMonths()).deadline(listing.getDeadline())
                .status(listing.getStatus()).createdAt(listing.getCreatedAt()).postedById(postedBy.getId())
                .postedByRole(postedBy.getRole().name()).applicantsCount(applicationRepository.countByInternshipId(listing.getId()))
                .isSaved(saved).hasApplied(applied).build();
    }

    private InternshipApplicationResponse mapToAppResponse(InternshipApplication app) {
        User learner = app.getLearner();
        return InternshipApplicationResponse.builder().id(app.getId())
                .internshipId(app.getInternship().getId()).internshipTitle(app.getInternship().getTitle())
                .learnerId(learner.getId()).learnerName(learner.getFullName()).learnerEmail(learner.getEmail())
                .learnerPhone(learner.getPhone()).university(learner.getUniversity()).department(learner.getDepartment())
                .coverLetter(app.getCoverLetter()).resumeUri(app.getResumeUrl())
                .organizationName(app.getInternship().getOrganization().getName()).status(app.getStatus())
                .appliedAt(app.getAppliedAt()).notes(app.getNotes()).build();
    }

    private boolean ownsListing(InternshipListing listing, User actor) {
        return actor != null && (actor.getRole() == UserRole.SUPER_ADMIN
                || listing.getOrganization().getOwner().getId().equals(actor.getId())
                || listing.getCoordinator() != null && listing.getCoordinator().getId().equals(actor.getId()));
    }

    private boolean isPubliclyAvailable(InternshipListing listing) {
        return listing.getStatus() == InternshipStatus.PUBLISHED
                && listing.getOrganization().getVerificationStatus() == VerificationStatus.APPROVED
                && listing.getDeadline() != null && listing.getDeadline().isAfter(LocalDateTime.now());
    }

    private void assertCanManage(InternshipListing listing, User actor) {
        if (!ownsListing(listing, actor)) throw new AccessDeniedException("You do not own this internship listing");
    }

    private void requireApprovedCoordinator(User actor) {
        if (actor != null && actor.getRole() == UserRole.INTERNSHIP_COORDINATOR
                && actor.getCoordinatorStatus() != ApprovalStatus.APPROVED) {
            throw new AccessDeniedException("Coordinator approval is required for this action");
        }
    }
}
