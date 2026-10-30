package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.CourseLevel;
import com.AddisBuilders.Platform.Model.enums.CourseStatus;

public record UpdateCourseRequest(
        String title,
        String description,
        String shortDescription,
        CourseLevel level,
        Integer durationWeeks,
        String learningObjectives,
        String thumbnailUrl,
        Long categoryId,
        CourseStatus status
) {}