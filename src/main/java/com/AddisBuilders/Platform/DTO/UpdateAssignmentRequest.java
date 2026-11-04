package com.AddisBuilders.Platform.DTO;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Partial update for an assignment. Every field is optional; only supplied
 * fields are applied. Due dates in the past are allowed so mentors can correct
 * or extend an existing assignment.
 */
public record UpdateAssignmentRequest(
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        String title,
        String description,
        LocalDateTime dueDate,
        @Positive(message = "Max points must be greater than zero")
        Integer maxPoints,
        @Size(max = 1000, message = "Attachment URL cannot exceed 1000 characters")
        String attachmentUrl
) {}
