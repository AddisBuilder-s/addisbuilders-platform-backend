package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.ContentType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request payload for editing an existing lesson. Every field is optional so
 * mentors can patch just the title, body, media URL, order or duration.
 */
public record UpdateLessonRequest(
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        String title,

        ContentType type,

        String body,

        @Size(max = 1000, message = "Content URL cannot exceed 1000 characters")
        String contentUrl,

        @Positive(message = "Duration must be a positive number of minutes")
        Integer durationMinutes,

        @Positive(message = "Lesson order must be a positive integer")
        Integer lessonOrder
) {}
