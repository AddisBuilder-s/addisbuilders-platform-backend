package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCourseRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        String shortDescription,
        CourseLevel level,
        @Positive
        Integer durationWeeks,
        String learningObjectives,
        Long categoryId,
        String thumbnailUrl
) {}
