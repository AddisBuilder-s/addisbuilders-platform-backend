package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.CourseLevel;
import jakarta.validation.constraints.NotBlank;

public record CreateCourseRequest(
        @NotBlank String title,
        String description,
        String shortDescription,
        CourseLevel level,
        Integer durationWeeks,
        String learningObjectives,
        Long categoryId,
        String thumbnailUrl
) {}