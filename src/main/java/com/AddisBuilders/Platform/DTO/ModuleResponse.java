package com.AddisBuilders.Platform.DTO;

import lombok.Builder;
import java.util.List;

/**
 * Data Transfer Object representing a module and its nested lessons.
 */
@Builder
public record ModuleResponse(
        Long id,
        Long courseId,
        String title,
        Integer moduleOrder,
        List<LessonResponse> lessons
) {}