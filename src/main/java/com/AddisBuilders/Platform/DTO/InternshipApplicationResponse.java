package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.ApplicationStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InternshipApplicationResponse(
        Long id,
        Long internshipId,
        String internshipTitle,
        Long learnerId,
        String learnerName,
        String coverLetter,
        String resumeUri,
        ApplicationStatus status,
        LocalDateTime appliedAt
) {}
