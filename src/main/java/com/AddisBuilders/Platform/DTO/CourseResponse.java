package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.CourseLevel;
import com.AddisBuilders.Platform.Model.enums.CourseStatus;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing the public output of a Course entity.
 * Decouples database entities from API client requirements.
 */
@Builder
public record CourseResponse(
        Long id,
        String title,
        String description,
        CourseLevel level,
        CourseStatus status,
        String thumbnailUrl,
        Long mentorId,
        String mentorName,
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt
) {}