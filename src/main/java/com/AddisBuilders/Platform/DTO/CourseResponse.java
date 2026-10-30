package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.CourseLevel;
import com.AddisBuilders.Platform.Model.enums.CourseStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CourseResponse(
        Long id,
        String title,
        String description,
        String shortDescription,
        CourseLevel level,
        CourseStatus status,
        String thumbnailUrl,
        Integer durationWeeks,
        String learningObjectives,
        Long mentorId,
        String instructorName, // Mapped to mentor.fullName so frontend card displays it correctly
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt
) {}