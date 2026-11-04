package com.AddisBuilders.Platform.DTO;

import lombok.Builder;

@Builder
public record AdminReportResponse(
        long totalLearners, long totalMentors, long totalCourses, long totalInternships,
        long totalApplications, long totalOrganizations, long verifiedOrganizations,
        long pendingApprovals, long pendingVerifications, long publishedCourses,
        long activeEnrollments, double completionRate, long submissionsCount, long gradesPending
) {}
