package com.AddisBuilders.Platform.DTO;

import lombok.Builder;

@Builder
public record LeaderboardEntryResponse(
        int rank,
        Long learnerId,
        String learnerName,
        String university,
        String department,
        long completedLessons,
        long gradedSubmissions,
        double averageGrade,
        long points
) {}
