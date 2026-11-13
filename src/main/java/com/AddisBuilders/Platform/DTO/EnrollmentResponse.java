package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.EnrollmentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EnrollmentResponse(
        Long id,
        Long studentId,
        String studentName,
        String studentAvatarUrl,
        String studentEmail,
        String studentUniversity,
        String studentDepartment,
        Long courseId,
        String courseTitle,
        String instructorId,
        EnrollmentStatus status,
        LocalDateTime enrolledAt
) {}