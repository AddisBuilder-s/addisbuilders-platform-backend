package com.AddisBuilders.Platform.DTO;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record AssignmentResponse(
        Long id,
        Long courseId,
        String courseTitle,
        String title,
        String description,
        LocalDateTime dueDate,
        Integer maxPoints,
        String attachmentUrl,
        LocalDateTime createdAt
) {}