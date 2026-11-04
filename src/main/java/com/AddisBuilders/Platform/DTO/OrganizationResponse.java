package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.VerificationStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrganizationResponse(
        Long id,
        Long ownerId,
        String name,
        String industry,
        String description,
        String website,
        String location,
        String contactEmail,
        String contactPhone,
        String registrationNumber,
        String taxId,
        VerificationStatus verificationStatus,
        String verificationNotes,
        String logoUrl,
        String verificationDocumentName,
        String verificationDocumentUrl,
        LocalDateTime createdAt
) {}
