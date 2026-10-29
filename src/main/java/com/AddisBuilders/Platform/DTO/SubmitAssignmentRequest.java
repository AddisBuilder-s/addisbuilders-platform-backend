package com.AddisBuilders.Platform.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for a student submitting their assignment work.
 */
public record SubmitAssignmentRequest(
        String textAnswer,

        @Size(max = 1000, message = "File URL cannot exceed 1000 characters")
        String fileUrl
) {}