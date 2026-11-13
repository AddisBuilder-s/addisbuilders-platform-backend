package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.CourseLevel;
import com.AddisBuilders.Platform.Model.enums.CourseStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

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
        String instructorAvatar, // Mapped to mentor.avatarUrl for course listing/detail cards
        Long categoryId,
        String categoryName,
        List<MaterialResponse> materials,
        LocalDateTime createdAt
) {}