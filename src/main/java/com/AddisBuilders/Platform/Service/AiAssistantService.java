package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.AiAnswerResponse;
import com.AddisBuilders.Platform.DTO.InternshipListingResponse;
import com.AddisBuilders.Platform.DTO.InternshipMatchResponse;
import com.AddisBuilders.Platform.DTO.LearnerDashboardResponse;
import com.AddisBuilders.Platform.DTO.ProgressSummaryResponse;
import com.AddisBuilders.Platform.DTO.StudyPlanResponse;
import com.AddisBuilders.Platform.Exception.ResourceNotFoundException;
import com.AddisBuilders.Platform.Model.Assignment;
import com.AddisBuilders.Platform.Model.Course;
import com.AddisBuilders.Platform.Model.Lesson;
import com.AddisBuilders.Platform.Model.Module;
import com.AddisBuilders.Platform.Model.StudentProgress;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.CourseStatus;
import com.AddisBuilders.Platform.Model.enums.EnrollmentStatus;
import com.AddisBuilders.Platform.Repository.AssignmentRepository;
import com.AddisBuilders.Platform.Repository.CourseRepository;
import com.AddisBuilders.Platform.Repository.EnrollmentRepository;
import com.AddisBuilders.Platform.Repository.LessonRepository;
import com.AddisBuilders.Platform.Repository.ModuleRepository;
import com.AddisBuilders.Platform.Repository.StudentProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deterministic, dependency-free "AI" helpers. They intentionally use simple
 * heuristics over platform data so the feature works without an external LLM
 * provider key. The contract mirrors what a real LLM service would return, so
 * the implementation can later be swapped behind the same endpoints.
 */
@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private static final int WEEKLY_MINUTES = 300;

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final StudentProgressRepository progressRepository;
    private final AssignmentRepository assignmentRepository;
    private final DashboardService dashboardService;
    private final InternshipService internshipService;

    @Transactional(readOnly = true)
    public StudyPlanResponse studyPlan(Long courseId, User learner) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        requireEnrollment(learner, courseId);

        List<Module> modules = moduleRepository.findByCourseIdOrderByModuleOrderAsc(courseId);
        List<Lesson> lessons = modules.stream()
                .flatMap(module -> lessonRepository.findByModuleIdOrderByLessonOrderAsc(module.getId()).stream())
                .toList();
        List<Long> lessonIds = lessons.stream().map(Lesson::getId).toList();
        Set<Long> completed = progressRepository.findByStudentIdAndLessonIdIn(learner.getId(), lessonIds).stream()
                .filter(item -> Boolean.TRUE.equals(item.getCompleted()))
                .map(item -> item.getLesson().getId())
                .collect(Collectors.toSet());

        List<Lesson> remaining = lessons.stream()
                .filter(lesson -> !completed.contains(lesson.getId()))
                .toList();
        int averageMinutes = (int) Math.max(20, lessons.stream()
                .filter(lesson -> lesson.getDurationMinutes() != null)
                .mapToInt(Lesson::getDurationMinutes)
                .average()
                .orElse(30));
        int lessonsPerWeek = Math.max(1, WEEKLY_MINUTES / averageMinutes);
        int estimatedWeeks = (int) Math.ceil((double) remaining.size() / lessonsPerWeek);

        List<StudyPlanResponse.StudyPlanItem> items = new ArrayList<>();
        for (int index = 0; index < remaining.size(); index++) {
            Lesson lesson = remaining.get(index);
            Module module = lesson.getModule();
            items.add(StudyPlanResponse.StudyPlanItem.builder()
                    .lessonId(lesson.getId())
                    .lessonTitle(lesson.getTitle())
                    .moduleId(module.getId())
                    .moduleTitle(module.getTitle())
                    .durationMinutes(lesson.getDurationMinutes())
                    .week((index / lessonsPerWeek) + 1)
                    .status("NOT_STARTED")
                    .build());
        }

        return StudyPlanResponse.builder()
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .totalLessons(lessons.size())
                .remainingLessons(remaining.size())
                .estimatedWeeks(estimatedWeeks)
                .weeklyMinutes(WEEKLY_MINUTES)
                .items(items)
                .build();
    }

    @Transactional(readOnly = true)
    public ProgressSummaryResponse progressSummary(User learner) {
        LearnerDashboardResponse stats = dashboardService.learnerSummary(learner);

        List<String> strengths = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        if (stats.overallCompletionPercentage() >= 50) {
            strengths.add("You have completed more than half of your enrolled lesson content.");
        }
        if (stats.averageGrade() >= 80 && stats.gradedSubmissions() > 0) {
            strengths.add("Your graded work is averaging " + stats.averageGrade() + "%.");
        }
        if (stats.completedCourses() > 0) {
            strengths.add("You have completed " + stats.completedCourses() + " course(s) end to end.");
        }
        if (stats.pendingAssignments() > 0) {
            recommendations.add("You have " + stats.pendingAssignments()
                    + " assignment(s) pending. Submit the nearest deadline first.");
        }
        if (stats.gradedSubmissions() > 0 && stats.averageGrade() < 70) {
            recommendations.add("Review mentor feedback on graded submissions before attempting the next one.");
        }
        if (stats.overallCompletionPercentage() < 50 && stats.totalLessons() > 0) {
            recommendations.add("Schedule two short lesson blocks this week to move past the halfway point.");
        }
        if (stats.internshipApplications() == 0) {
            recommendations.add("Browse the internship catalogue and submit one application to start your pipeline.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Keep your current pace and consider saving an advanced course for next term.");
        }

        String summary = String.format(
                "%s is enrolled in %d course(s), has completed %d of %d lessons (%.2f%%), "
                        + "submitted %d assignment(s) with an average grade of %.2f%%, "
                        + "and has %d internship application(s).",
                learner.getFullName(), stats.enrolledCourses(), stats.completedLessons(), stats.totalLessons(),
                stats.overallCompletionPercentage(), stats.submittedAssignments(), stats.averageGrade(),
                stats.internshipApplications());

        return ProgressSummaryResponse.builder()
                .learnerId(learner.getId())
                .summary(summary)
                .enrolledCourses(stats.enrolledCourses())
                .completedLessons(stats.completedLessons())
                .totalLessons(stats.totalLessons())
                .completionPercentage(stats.overallCompletionPercentage())
                .gradedSubmissions(stats.gradedSubmissions())
                .averageGrade(stats.averageGrade())
                .strengths(strengths)
                .recommendations(recommendations)
                .build();
    }

    @Transactional(readOnly = true)
    public List<InternshipMatchResponse> internshipMatches(User learner, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        List<InternshipListingResponse> listings = internshipService.getPublishedListings(learner);

        Set<String> interests = new HashSet<>();
        for (var enrollment : enrollmentRepository.findByStudentId(learner.getId())) {
            Course course = enrollment.getCourse();
            if (course.getCategory() != null) {
                interests.addAll(tokens(course.getCategory().getName()));
            }
            interests.addAll(tokens(course.getTitle()));
        }
        if (learner.getDepartment() != null) {
            interests.addAll(tokens(learner.getDepartment()));
        }

        return listings.stream()
                .map(listing -> {
                    Set<String> listingTokens = new HashSet<>();
                    listingTokens.addAll(tokens(listing.title()));
                    listingTokens.addAll(tokens(listing.category()));
                    listingTokens.addAll(tokens(listing.description()));
                    listingTokens.addAll(tokens(listing.requirements()));
                    listingTokens.addAll(tokens(listing.eligibility()));

                    List<String> matched = interests.stream()
                            .filter(listingTokens::contains)
                            .sorted()
                            .limit(4)
                            .toList();
                    double score = 40.0 + Math.min(40, matched.size() * 10);
                    if (listing.category() != null) {
                        score += 10;
                    }
                    if (learner.getDepartment() != null && listing.eligibility() != null
                            && listing.eligibility().toLowerCase(Locale.ROOT)
                            .contains(learner.getDepartment().toLowerCase(Locale.ROOT))) {
                        score += 10;
                    }
                    score = Math.min(100.0, score);

                    List<String> reasons = new ArrayList<>();
                    if (!matched.isEmpty()) {
                        reasons.add("Matches your interests: " + String.join(", ", matched));
                    }
                    if (listing.category() != null) {
                        reasons.add("Category: " + listing.category());
                    }
                    reasons.add("Work mode: " + listing.workMode());
                    if (listing.location() != null) {
                        reasons.add("Location: " + listing.location());
                    }

                    return InternshipMatchResponse.builder()
                            .internshipId(listing.id())
                            .title(listing.title())
                            .organizationName(listing.organizationName())
                            .category(listing.category())
                            .workMode(listing.workMode() == null ? null : listing.workMode().name())
                            .location(listing.location())
                            .matchScore(score)
                            .reasons(reasons)
                            .build();
                })
                .sorted(Comparator.comparingDouble(InternshipMatchResponse::matchScore).reversed())
                .limit(safeLimit)
                .toList();
    }

    @Transactional(readOnly = true)
    public AiAnswerResponse ask(String question, Long courseId, User learner) {
        String normalized = question.toLowerCase(Locale.ROOT);
        List<String> sources = new ArrayList<>();
        StringBuilder answer = new StringBuilder();

        if (courseId != null) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
            boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndStatusIn(
                    learner.getId(), courseId, List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED));
            if (course.getStatus() != CourseStatus.PUBLISHED && !enrolled) {
                throw new AccessDeniedException("This course is not published yet");
            }

            if (normalized.contains("assignment") || normalized.contains("deadline") || normalized.contains("due")) {
                List<Assignment> assignments = assignmentRepository.findByCourseId(courseId);
                answer.append("This course has ").append(assignments.size()).append(" assignment(s). ");
                assignments.stream().limit(5).forEach(assignment ->
                        answer.append(assignment.getTitle()).append(" (due ")
                                .append(assignment.getDueAt()).append(", ")
                                .append(assignment.getPoints()).append(" points). "));
                sources.add("course assignments");
            } else {
                answer.append("About ").append(course.getTitle()).append(": ")
                        .append(course.getDescription() == null ? "No description provided." : course.getDescription())
                        .append(" ");
                if (course.getLearningObjectives() != null) {
                    answer.append("Learning objectives: ").append(course.getLearningObjectives()).append(" ");
                }
                List<Module> modules = moduleRepository.findByCourseIdOrderByModuleOrderAsc(courseId);
                List<String> lessonTitles = modules.stream()
                        .flatMap(module -> lessonRepository.findByModuleIdOrderByLessonOrderAsc(module.getId()).stream())
                        .map(Lesson::getTitle)
                        .limit(10)
                        .toList();
                if (!lessonTitles.isEmpty()) {
                    answer.append("The curriculum covers: ").append(String.join(", ", lessonTitles)).append(".");
                }
                sources.add("course description");
                sources.add("course curriculum");
            }
        } else {
            ProgressSummaryResponse summary = progressSummary(learner);
            answer.append(summary.summary()).append(" ");
            if (!summary.recommendations().isEmpty()) {
                answer.append("Recommended next steps: ")
                        .append(String.join(" ", summary.recommendations()));
            }
            sources.add("learner progress data");
        }

        return AiAnswerResponse.builder()
                .answer(answer.toString().trim())
                .sources(sources)
                .aiGenerated(false)
                .build();
    }

    private void requireEnrollment(User learner, Long courseId) {
        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndStatusIn(
                learner.getId(), courseId, List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED));
        if (!enrolled) {
            throw new AccessDeniedException("You must be enrolled in this course to generate a study plan");
        }
    }

    private Set<String> tokens(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return java.util.Arrays.stream(value.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(token -> token.length() >= 3)
                .filter(token -> !Set.of("the", "and", "for", "with", "your", "you", "this", "that", "are", "from")
                        .contains(token))
                .collect(Collectors.toSet());
    }
}
