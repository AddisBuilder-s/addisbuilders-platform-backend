package com.AddisBuilders.Platform.DTO;

public record LessonCompletionResponse(
        double progressPercentage,
        long completedCount
) {}