package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.ContentType;
import lombok.Builder;

/**
 * Data Transfer Object representing an individual lesson.
 */
@Builder
public record LessonResponse(
        Long id,
        Long moduleId,
        String title,
        ContentType type,
        String body,
        String contentUrl,
        Integer lessonOrder
) {}