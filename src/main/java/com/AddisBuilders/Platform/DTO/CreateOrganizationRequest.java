package com.AddisBuilders.Platform.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateOrganizationRequest(
        @NotBlank(message = "Organization name is required")
        @Size(max = 200, message = "Name cannot exceed 200 characters")
        String name,

        String description,
        String website,
        String logoUrl
) {}
