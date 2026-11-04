package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.CourseLevel;
import com.AddisBuilders.Platform.Model.enums.CourseStatus;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateCourseRequest(
        @Size(max = 200) String title,
        String description,
        String shortDescription,
        CourseLevel level,
        @Positive Integer durationWeeks,
        String learningObjectives,
        String thumbnailUrl,
        Long categoryId,
        CourseStatus status
) {}
