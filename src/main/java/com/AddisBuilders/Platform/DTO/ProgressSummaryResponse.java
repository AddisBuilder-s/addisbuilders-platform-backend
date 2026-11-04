package com.AddisBuilders.Platform.DTO;

import lombok.Builder;

import java.util.List;

@Builder
public record ProgressSummaryResponse(
        Long learnerId,
        String summary,
        long enrolledCourses,
        long completedLessons,
        long totalLessons,
        double completionPercentage,
        long gradedSubmissions,
        double averageGrade,
        List<String> strengths,
        List<String> recommendations
) {}
