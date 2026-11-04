package com.AddisBuilders.Platform.DTO;

import lombok.Builder;

@Builder
public record LearnerDashboardResponse(
        Long learnerId,
        String learnerName,
        long enrolledCourses,
        long activeCourses,
        long completedCourses,
        long totalLessons,
        long completedLessons,
        double overallCompletionPercentage,
        long pendingAssignments,
        long submittedAssignments,
        long gradedSubmissions,
        double averageGrade,
        long savedInternships,
        long internshipApplications,
        long unreadNotifications
) {}
