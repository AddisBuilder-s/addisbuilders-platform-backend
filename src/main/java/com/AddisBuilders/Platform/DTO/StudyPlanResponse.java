package com.AddisBuilders.Platform.DTO;

import lombok.Builder;

import java.util.List;

@Builder
public record StudyPlanResponse(
        Long courseId,
        String courseTitle,
        int totalLessons,
        int remainingLessons,
        int estimatedWeeks,
        int weeklyMinutes,
        List<StudyPlanItem> items
) {
    @Builder
    public record StudyPlanItem(
            Long lessonId,
            String lessonTitle,
            Long moduleId,
            String moduleTitle,
            Integer durationMinutes,
            int week,
            String status
    ) {}
}
