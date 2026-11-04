package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.LearnerDashboardResponse;
import com.AddisBuilders.Platform.DTO.MentorDashboardResponse;
import com.AddisBuilders.Platform.Model.Assignment;
import com.AddisBuilders.Platform.Model.Course;
import com.AddisBuilders.Platform.Model.Enrollment;
import com.AddisBuilders.Platform.Model.Module;
import com.AddisBuilders.Platform.Model.Submission;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.CourseStatus;
import com.AddisBuilders.Platform.Model.enums.EnrollmentStatus;
import com.AddisBuilders.Platform.Model.enums.SubmissionStatus;
import com.AddisBuilders.Platform.Repository.AssignmentRepository;
import com.AddisBuilders.Platform.Repository.CourseRepository;
import com.AddisBuilders.Platform.Repository.EnrollmentRepository;
import com.AddisBuilders.Platform.Repository.InternshipApplicationRepository;
import com.AddisBuilders.Platform.Repository.LessonRepository;
import com.AddisBuilders.Platform.Repository.ModuleRepository;
import com.AddisBuilders.Platform.Repository.NotificationRepository;
import com.AddisBuilders.Platform.Repository.SavedInternshipRepository;
import com.AddisBuilders.Platform.Repository.StudentProgressRepository;
import com.AddisBuilders.Platform.Repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Aggregates the numbers rendered by the mentor and learner dashboards so the
 * frontend can load one summary endpoint instead of stitching together many
 * list calls.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final StudentProgressRepository progressRepository;
    private final SavedInternshipRepository savedInternshipRepository;
    private final InternshipApplicationRepository applicationRepository;
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public MentorDashboardResponse mentorSummary(User mentor) {
        List<Course> courses = courseRepository.findByMentorId(mentor.getId());
        Set<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toSet());
        List<Enrollment> enrollments = enrollmentRepository.findByCourseMentorId(mentor.getId());

        List<Assignment> assignments = assignmentRepository.findAll().stream()
                .filter(assignment -> assignment.getCourse().getMentor().getId().equals(mentor.getId()))
                .toList();

        Collection<Submission> submissions = submissionRepository.findByAssignmentCourseMentorId(mentor.getId());
        long pending = submissions.stream()
                .filter(item -> item.getStatus() == SubmissionStatus.SUBMITTED
                        || item.getStatus() == SubmissionStatus.UNDER_REVIEW)
                .count();
        long graded = submissions.stream()
                .filter(item -> item.getStatus() == SubmissionStatus.GRADED)
                .count();

        LocalDateTime now = LocalDateTime.now();
        long upcoming = assignments.stream()
                .filter(item -> item.getDueAt() != null
                        && item.getDueAt().isAfter(now)
                        && item.getDueAt().isBefore(now.plusDays(7)))
                .count();

        long activeStudents = enrollments.stream()
                .filter(item -> item.getStatus() == EnrollmentStatus.ACTIVE)
                .map(item -> item.getStudent().getId())
                .distinct()
                .count();
        long completedStudents = enrollments.stream()
                .filter(item -> item.getStatus() == EnrollmentStatus.COMPLETED)
                .map(item -> item.getStudent().getId())
                .distinct()
                .count();

        return MentorDashboardResponse.builder()
                .mentorId(mentor.getId())
                .mentorName(mentor.getFullName())
                .totalCourses(courses.size())
                .publishedCourses(courses.stream().filter(c -> c.getStatus() == CourseStatus.PUBLISHED).count())
                .draftCourses(courses.stream().filter(c -> c.getStatus() == CourseStatus.DRAFT).count())
                .totalStudents(enrollments.stream().map(e -> e.getStudent().getId()).distinct().count())
                .activeStudents(activeStudents)
                .completedStudents(completedStudents)
                .totalAssignments(assignments.size())
                .upcomingAssignments(upcoming)
                .pendingSubmissions(pending)
                .gradedSubmissions(graded)
                .averageGrade(averageGrade(submissions))
                .unreadNotifications(notificationRepository.countByUserIdAndReadFalse(mentor.getId()))
                .build();
    }

    @Transactional(readOnly = true)
    public LearnerDashboardResponse learnerSummary(User learner) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(learner.getId());
        Set<Long> courseIds = enrollments.stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .collect(Collectors.toSet());

        long totalLessons = 0;
        for (Long courseId : courseIds) {
            List<Module> modules = moduleRepository.findByCourseIdOrderByModuleOrderAsc(courseId);
            totalLessons += modules.stream()
                    .mapToLong(module -> lessonRepository.findByModuleIdOrderByLessonOrderAsc(module.getId()).size())
                    .sum();
        }
        long completedLessons = progressRepository.countByStudentIdAndCompletedTrue(learner.getId());

        List<Assignment> assignments = assignmentRepository.findAll().stream()
                .filter(assignment -> courseIds.contains(assignment.getCourse().getId()))
                .toList();
        List<Submission> submissions = submissionRepository.findByLearnerId(learner.getId());
        Set<Long> submittedAssignmentIds = submissions.stream()
                .map(submission -> submission.getAssignment().getId())
                .collect(Collectors.toSet());
        LocalDateTime now = LocalDateTime.now();
        long pendingAssignments = assignments.stream()
                .filter(assignment -> !submittedAssignmentIds.contains(assignment.getId()))
                .filter(assignment -> assignment.getDueAt() == null || assignment.getDueAt().isAfter(now))
                .count();

        long graded = submissions.stream()
                .filter(item -> item.getStatus() == SubmissionStatus.GRADED)
                .count();
        double completion = totalLessons == 0
                ? 0.0
                : round(((double) completedLessons / totalLessons) * 100.0);

        return LearnerDashboardResponse.builder()
                .learnerId(learner.getId())
                .learnerName(learner.getFullName())
                .enrolledCourses(enrollments.size())
                .activeCourses(enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE).count())
                .completedCourses(enrollments.stream().filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED).count())
                .totalLessons(totalLessons)
                .completedLessons(completedLessons)
                .overallCompletionPercentage(completion)
                .pendingAssignments(pendingAssignments)
                .submittedAssignments(submissions.size())
                .gradedSubmissions(graded)
                .averageGrade(averageGrade(submissions))
                .savedInternships(savedInternshipRepository.findByUserIdOrderByCreatedAtDesc(learner.getId()).size())
                .internshipApplications(applicationRepository.findByLearnerId(learner.getId()).size())
                .unreadNotifications(notificationRepository.countByUserIdAndReadFalse(learner.getId()))
                .build();
    }

    static double averageGrade(Collection<Submission> submissions) {
        List<BigDecimal> grades = submissions.stream()
                .map(Submission::getGrade)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (grades.isEmpty()) {
            return 0.0;
        }
        BigDecimal total = grades.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP).doubleValue();
    }

    static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
