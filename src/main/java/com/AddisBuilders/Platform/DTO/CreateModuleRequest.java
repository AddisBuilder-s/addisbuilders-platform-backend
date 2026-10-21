package com.AddisBuilders.Platform.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a curriculum module inside a course.
 */
public record CreateModuleRequest(
        @NotBlank(message = "Module title is required")
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        String title,

        @NotNull(message = "Module order sequence is required")
        @Positive(message = "Module order must be a positive integer")
        Integer moduleOrder
) {}