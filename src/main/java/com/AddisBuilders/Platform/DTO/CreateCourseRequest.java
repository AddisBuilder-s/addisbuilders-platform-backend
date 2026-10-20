package com.AddisBuilders.Platform.DTO;
import com.AddisBuilders.Platform.Model.enums.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating or updating a Course entity.
 * Uses immutable Java Record pattern to enforce clean boundary contracts.
 */
public record CreateCourseRequest(
        // Mandatory title with character limit matching DB schema constraints
        @NotBlank(message = "Course title is required")
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        String title,

        // Detailed course summary
        String description,

        // Skill level tag (BEGINNER, INTERMEDIATE, ADVANCED, ALL_LEVELS)
        @NotNull(message = "Course level is required")
        CourseLevel level,

        // Foreign key reference to CourseCategory
        Long categoryId,

        // Thumbnail image asset URL
        String thumbnailUrl
) {}