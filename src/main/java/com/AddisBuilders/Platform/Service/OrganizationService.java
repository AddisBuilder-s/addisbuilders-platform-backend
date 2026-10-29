package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.CreateOrganizationRequest;
import com.AddisBuilders.Platform.DTO.OrganizationResponse;
import com.AddisBuilders.Platform.Model.Organization;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.VerificationStatus;
import com.AddisBuilders.Platform.Repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
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
        // Map DTO payload to Organization JPA Entity[cite: 1, 2]
        Organization organization = Organization.builder()
                .owner(currentUser) // Assign foreign key reference user_id[cite: 1, 2]
                .name(request.name())
                .description(request.description())
                .website(request.website())
                .logoUrl(request.logoUrl())
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
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Converts Organization JPA entity into a clean response DTO[cite: 1, 2].
     */
    private OrganizationResponse mapToResponse(Organization org) {
        return OrganizationResponse.builder()
                .id(org.getId())
                .ownerId(org.getOwner().getId())
                .name(org.getName())
                .description(org.getDescription())
                .website(org.getWebsite())
                .verificationStatus(org.getVerificationStatus())
                .logoUrl(org.getLogoUrl())
                .createdAt(org.getCreatedAt())
                .build();
    }
}