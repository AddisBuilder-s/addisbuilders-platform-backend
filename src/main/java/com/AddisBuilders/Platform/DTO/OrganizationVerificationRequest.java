package com.AddisBuilders.Platform.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizationVerificationRequest(
        @NotBlank @Size(max = 150) String registrationNumber,
        @NotBlank @Size(max = 150) String taxId,
        @NotBlank @Size(max = 255) String documentName,
        @NotBlank @Size(max = 1000) String documentUrl,
        String notes
) {}
