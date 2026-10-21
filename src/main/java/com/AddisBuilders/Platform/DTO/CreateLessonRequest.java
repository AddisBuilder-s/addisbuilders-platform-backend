package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating an individual lesson within a module.
 */
public record CreateLessonRequest(
        @NotBlank(message = "Lesson title is required")
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        String title,

        @NotNull(message = "Lesson type is required")
        ContentType type,

        String body,

        String contentUrl,

        @NotNull(message = "Lesson order sequence is required")
        @Positive(message = "Lesson order must be a positive integer")
        Integer lessonOrder
) {}