package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.InternshipStatus;
import com.AddisBuilders.Platform.Model.enums.WorkMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateInternshipListingRequest(
        @Size(max = 200) String title,
        String description,
        String requirements,
        WorkMode workMode,
        @Size(max = 255) String location,
        @Size(max = 150) String category,
        String responsibilities,
        String eligibility,
        @Size(max = 150) String stipend,
        @Positive Integer durationMonths,
        @Future LocalDateTime deadline,
        InternshipStatus status
) {}
