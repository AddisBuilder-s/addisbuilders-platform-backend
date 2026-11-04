package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.AdminReportResponse;
import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;
import com.AddisBuilders.Platform.Model.enums.EnrollmentStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Model.enums.VerificationStatus;
import com.AddisBuilders.Platform.Model.Enrollment;
import com.AddisBuilders.Platform.Model.Lesson;
import com.AddisBuilders.Platform.Repository.ModuleRepository;
import com.AddisBuilders.Platform.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportService {
    private final UserRepository users;
    private final CourseRepository courses;
    private final InternshipListingRepository internships;
    private final InternshipApplicationRepository applications;
    private final OrganizationRepository organizations;
    private final EnrollmentRepository enrollments;
    private final SubmissionRepository submissions;
    private final StudentProgressRepository progress;
    private final LessonRepository lessons;
    private final ModuleRepository modules;

    @Transactional(readOnly = true)
    public AdminReportResponse overview() {
        var allUsers = users.findAll();
        long learners = allUsers.stream().filter(user -> user.getRole() == UserRole.LEARNER).count();
        long mentors = allUsers.stream().filter(user -> user.getRole() == UserRole.MENTOR).count();
        var allEnrollments = enrollments.findAll();
        long activeEnrollments = allEnrollments.stream().filter(item -> item.getStatus() == EnrollmentStatus.ACTIVE).count();
        long totalLessonSlots = 0;
        long completedLessonSlots = 0;
        for (Enrollment enrollment : allEnrollments) {
            if (enrollment.getStatus() != EnrollmentStatus.ACTIVE && enrollment.getStatus() != EnrollmentStatus.COMPLETED) continue;
            var lessonIds = modules.findByCourseIdOrderByModuleOrderAsc(enrollment.getCourse().getId()).stream()
                    .flatMap(module -> lessons.findByModuleIdOrderByLessonOrderAsc(module.getId()).stream())
                    .map(Lesson::getId).toList();
            totalLessonSlots += lessonIds.size();
            if (!lessonIds.isEmpty()) {
                completedLessonSlots += progress.findByStudentIdAndLessonIdIn(enrollment.getStudent().getId(), lessonIds).stream()
                        .filter(item -> Boolean.TRUE.equals(item.getCompleted())).count();
            }
        }
        double completionRate = totalLessonSlots == 0 ? 0
                : Math.round((double) completedLessonSlots / totalLessonSlots * 10000.0) / 100.0;
        long pendingApprovals = allUsers.stream()
                .filter(user -> user.getRole() == UserRole.INTERNSHIP_COORDINATOR
                        && user.getCoordinatorStatus() == ApprovalStatus.PENDING).count();
        long pendingVerifications = organizations.findAll().stream()
                .filter(org -> org.getVerificationStatus() == VerificationStatus.PENDING).count();
        long verifiedOrganizations = organizations.findAll().stream()
                .filter(org -> org.getVerificationStatus() == VerificationStatus.APPROVED).count();
        var allCourses = courses.findAll();
        long publishedCourses = allCourses.stream().filter(course -> course.getStatus().name().equals("PUBLISHED")).count();
        long gradePending = submissions.findAll().stream()
                .filter(submission -> submission.getStatus().name().equals("SUBMITTED")).count();
        return AdminReportResponse.builder().totalLearners(learners).totalMentors(mentors)
                .totalCourses(allCourses.size()).totalInternships(internships.count())
                .totalApplications(applications.count()).totalOrganizations(organizations.count())
                .verifiedOrganizations(verifiedOrganizations).publishedCourses(publishedCourses)
                .pendingApprovals(pendingApprovals)
                .pendingVerifications(pendingVerifications).activeEnrollments(activeEnrollments)
                .completionRate(completionRate).submissionsCount(submissions.count())
                .gradesPending(gradePending).build();
    }
}
