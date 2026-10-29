package com.AddisBuilders.Platform.DTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request payload for a mentor creating an assignment.
 */
public record CreateAssignmentRequest(
        @NotBlank(message = "Assignment title is required")
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        String title,

        String description,

        @NotNull(message = "Due date is required")
        @Future(message = "Due date must be in the future")
        LocalDateTime dueDate,

        @NotNull(message = "Max points is required")
        @Positive(message = "Max points must be greater than zero")
        Integer maxPoints

) {}