package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.Exception.ResourceNotFoundException;

import com.AddisBuilders.Platform.DTO.CreateOrganizationRequest;
import com.AddisBuilders.Platform.DTO.OrganizationVerificationRequest;
import com.AddisBuilders.Platform.DTO.OrganizationResponse;
import com.AddisBuilders.Platform.DTO.UpdateOrganizationRequest;
import com.AddisBuilders.Platform.DTO.VerificationDecisionRequest;
import com.AddisBuilders.Platform.Model.Organization;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.VerificationStatus;
import com.AddisBuilders.Platform.Repository.OrganizationRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service handling business logic for partner organizations[cite: 1, 2].
 * Manages organization creation and public directory listing.
 */
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final NotificationService notifications;
    private final AuditLogService auditLog;

    /**
     * Registers a new organization in the platform[cite: 1, 2].
     * Defaults the verification status to PENDING until approved by an administrator[cite: 1, 2, 3].
     *
     * @param request     DTO containing organization details (name, description, website, logoUrl)[cite: 1, 2].
     * @param currentUser Authenticated user initiating the creation (EMPLOYER or COORDINATOR)[cite: 1, 2, 3].
     * @return OrganizationResponse containing persistent entity attributes.
     */
    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request, User currentUser) {
        if (organizationRepository.existsByName(request.name())) {
            throw new com.AddisBuilders.Platform.Exception.ConflictException("An organization with this name already exists.");
        }
        if (organizationRepository.findByOwnerId(currentUser.getId()).isPresent()) {
            throw new com.AddisBuilders.Platform.Exception.ConflictException("You already have an organization profile.");
        }
        // Map DTO payload to Organization JPA Entity[cite: 1, 2]
        Organization organization = Organization.builder()
                .owner(currentUser) // Assign foreign key reference user_id[cite: 1, 2]
                .name(request.name())
                .description(request.description())
                .website(request.website())
                .logoUrl(request.logoUrl())
                .industry(request.industry())
                .location(request.location())
                .contactEmail(request.contactEmail())
                .contactPhone(request.contactPhone())
                .verificationStatus(VerificationStatus.PENDING) // Set initial verification status[cite: 1, 2, 3]
                .build();

        // Persist organization record into MySQL database
        Organization savedOrg = organizationRepository.save(organization);

        return mapToResponse(savedOrg);
    }

    /**
     * Fetches all registered organizations for student browsing or directory view[cite: 1, 2].
     *
     * @return List of OrganizationResponse objects.
     */
    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll()
                .stream()
                .filter(org -> org.getVerificationStatus() == VerificationStatus.APPROVED)
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAllOrganizationsAdmin() {
        return organizationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public OrganizationResponse getMyOrganization(User currentUser) {
        Organization org = organizationRepository.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> new com.AddisBuilders.Platform.Exception.ResourceNotFoundException("No organization profile found for the current user."));
        return mapToResponse(org);
    }

    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(Long id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));
        if (org.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new AccessDeniedException("Organization profile is not public");
        }
        return mapToResponse(org);
    }

    @Transactional
    public OrganizationResponse updateMyOrganization(User currentUser, UpdateOrganizationRequest request) {
        Organization org = organizationRepository.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> new com.AddisBuilders.Platform.Exception.ResourceNotFoundException("No organization profile found for the current user."));
        boolean legalIdentityChanged = request.name() != null && !request.name().equals(org.getName())
                || request.registrationNumber() != null && !request.registrationNumber().equals(org.getRegistrationNumber())
                || request.taxId() != null && !request.taxId().equals(org.getTaxId());
        if (request.name() != null) org.setName(request.name().trim());
        if (request.industry() != null) org.setIndustry(request.industry().trim());
        if (request.description() != null) org.setDescription(request.description());
        if (request.website() != null) org.setWebsite(request.website().trim());
        if (request.location() != null) org.setLocation(request.location().trim());
        if (request.contactEmail() != null) org.setContactEmail(request.contactEmail().trim());
        if (request.contactPhone() != null) org.setContactPhone(request.contactPhone().trim());
        if (request.registrationNumber() != null) org.setRegistrationNumber(request.registrationNumber().trim());
        if (request.taxId() != null) org.setTaxId(request.taxId().trim());
        if (legalIdentityChanged && org.getVerificationStatus() == VerificationStatus.APPROVED) {
            org.setVerificationStatus(VerificationStatus.PENDING);
        }
        return mapToResponse(organizationRepository.save(org));
    }

    @Transactional
    public OrganizationResponse submitVerification(User currentUser, OrganizationVerificationRequest request) {
        Organization org = organizationRepository.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> new com.AddisBuilders.Platform.Exception.ResourceNotFoundException("No organization profile found for the current user."));
        org.setRegistrationNumber(request.registrationNumber().trim());
        org.setTaxId(request.taxId().trim());
        org.setVerificationDocumentName(request.documentName().trim());
        org.setVerificationDocumentUrl(request.documentUrl().trim());
        org.setVerificationNotes(request.notes());
        org.setVerificationStatus(VerificationStatus.PENDING);
        return mapToResponse(organizationRepository.save(org));
    }

    @Transactional
    public OrganizationResponse decideVerification(Long id, VerificationDecisionRequest request,
                                                   User admin, HttpServletRequest servletRequest) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + id));
        if (request.status() == VerificationStatus.PENDING) {
            throw new IllegalArgumentException("Decision status cannot be PENDING");
        }
        org.setVerificationStatus(request.status());
        org.setVerificationNotes(request.notes());
        Organization saved = organizationRepository.save(org);
        notifications.notify(org.getOwner(), "Organization verification updated",
                "Your organization verification was " + request.status() + ".",
                request.status() == VerificationStatus.APPROVED ? "SUCCESS" : "WARNING",
                "/employer/verification");
        auditLog.record(admin, "ORGANIZATION_VERIFICATION", "ORGANIZATION", id,
                "Status set to " + request.status(), servletRequest);
        return mapToResponse(saved);
    }

    /**
     * Converts Organization JPA entity into a clean response DTO[cite: 1, 2].
     */
    private OrganizationResponse mapToResponse(Organization org) {
        return OrganizationResponse.builder()
                .id(org.getId())
                .ownerId(org.getOwner().getId())
                .name(org.getName())
                .industry(org.getIndustry())
                .description(org.getDescription())
                .website(org.getWebsite())
                .location(org.getLocation())
                .contactEmail(org.getContactEmail())
                .contactPhone(org.getContactPhone())
                .registrationNumber(org.getRegistrationNumber())
                .taxId(org.getTaxId())
                .verificationStatus(org.getVerificationStatus())
                .verificationNotes(org.getVerificationNotes())
                .logoUrl(org.getLogoUrl())
                .verificationDocumentName(org.getVerificationDocumentName())
                .verificationDocumentUrl(org.getVerificationDocumentUrl())
                .createdAt(org.getCreatedAt())
                .build();
    }
}
