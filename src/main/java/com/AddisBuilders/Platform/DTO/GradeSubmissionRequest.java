package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.SubmissionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Request payload for a mentor grading a student submission.
 */
public record GradeSubmissionRequest(
        @NotNull(message = "Grade is required")
        @PositiveOrZero(message = "Grade must be zero or positive")
        BigDecimal grade,

        String feedback,

        @NotNull(message = "Submission status is required")
        SubmissionStatus status
) {}