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
        String learnerEmail,
        String learnerPhone,
        String university,
        String department,
        String coverLetter,
        String resumeUri,
        String organizationName,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        String notes
) {}
