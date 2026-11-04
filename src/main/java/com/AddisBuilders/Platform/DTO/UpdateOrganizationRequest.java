package com.AddisBuilders.Platform.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
        @Size(max = 200) String name,
        @Size(max = 150) String industry,
        String description,
        @Size(max = 255) String website,
        @Size(max = 255) String location,
        @Email @Size(max = 255) String contactEmail,
        @Size(max = 40) String contactPhone,
        @Size(max = 150) String registrationNumber,
        @Size(max = 150) String taxId
) {}
