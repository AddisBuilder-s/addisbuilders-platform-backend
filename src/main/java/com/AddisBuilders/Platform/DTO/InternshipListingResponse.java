package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.InternshipStatus;
import com.AddisBuilders.Platform.Model.enums.WorkMode;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InternshipListingResponse(
        Long id,
        Long organizationId,
        String organizationName,
        String title,
        String description,
        String requirements,
        WorkMode workMode,
        LocalDateTime deadline,
        InternshipStatus status,
        LocalDateTime createdAt
) {}