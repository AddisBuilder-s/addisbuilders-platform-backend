package com.AddisBuilders.Platform.DTO;

import lombok.Builder;

@Builder
public record MentorDashboardResponse(
        Long mentorId,
        String mentorName,
        long totalCourses,
        long publishedCourses,
        long draftCourses,
        long totalStudents,
        long activeStudents,
        long completedStudents,
        long totalAssignments,
        long upcomingAssignments,
        long pendingSubmissions,
        long gradedSubmissions,
        double averageGrade,
        long unreadNotifications
) {}
