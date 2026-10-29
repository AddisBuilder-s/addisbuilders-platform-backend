package com.AddisBuilders.Platform.DTO;

import lombok.Builder;

/**
 * Response DTO returning a student's completion statistics for a course.
 */
@Builder
public record CourseProgressResponse(
        Long courseId,
        String courseTitle,
        long totalLessons,
        long completedLessons,
        double completionPercentage
) {}