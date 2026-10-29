package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.VerificationStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrganizationResponse(
        Long id,
        Long ownerId,
        String name,
        String description,
        String website,
        VerificationStatus verificationStatus,
        String logoUrl,
        LocalDateTime createdAt
) {}