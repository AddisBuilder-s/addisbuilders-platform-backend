package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.SubmissionStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO returning submission details and grading feedback.
 */
@Builder
public record SubmissionResponse(
        Long id,
        Long assignmentId,
        String assignmentTitle,
        Long learnerId,
        String learnerName,
        String textAnswer,
        String fileUrl,
        SubmissionStatus status,
        BigDecimal grade,
        String feedback,
        LocalDateTime submittedAt,
        LocalDateTime gradedAt
) {}