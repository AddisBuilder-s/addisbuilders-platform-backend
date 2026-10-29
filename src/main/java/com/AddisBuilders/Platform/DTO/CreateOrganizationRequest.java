package com.AddisBuilders.Platform.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank(message = "Organization name is required")
        @Size(max = 200, message = "Name cannot exceed 200 characters")
        String name,

        String description,
        String website,
        String logoUrl
) {}
