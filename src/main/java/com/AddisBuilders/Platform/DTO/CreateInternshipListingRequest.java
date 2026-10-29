package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.WorkMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateInternshipListingRequest(
        @NotNull(message = "Organization ID is required")
        Long organizationId,

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        String requirements,

        @NotNull(message = "Work mode is required")
        WorkMode workMode,

        @NotNull(message = "Deadline is required")
        @Future(message = "Deadline must be in the future")
        LocalDateTime deadline
) {}