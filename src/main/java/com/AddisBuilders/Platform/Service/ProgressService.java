package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.CourseProgressResponse;
import com.AddisBuilders.Platform.Model.Course;
import com.AddisBuilders.Platform.Model.Lesson;
import com.AddisBuilders.Platform.Model.Module;
import com.AddisBuilders.Platform.Model.StudentProgress;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.EnrollmentStatus;
import com.AddisBuilders.Platform.Repository.CourseRepository;
import com.AddisBuilders.Platform.Repository.EnrollmentRepository;
import com.AddisBuilders.Platform.Repository.LessonRepository;
import com.AddisBuilders.Platform.Repository.ModuleRepository;
import com.AddisBuilders.Platform.Repository.StudentProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing student lesson completion toggles and progress calculations.
 */
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final StudentProgressRepository progressRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final EnrollmentRepository enrollmentRepository;

    /**
     * Marks a specific lesson as completed for the authenticated student.
     */
    @Transactional
    public void markLessonComplete(Long courseId, Long lessonId, User student) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with ID: " + lessonId));

        Long lessonCourseId = lesson.getModule().getCourse().getId();
        if (!lessonCourseId.equals(courseId)) {
            throw new IllegalArgumentException("Lesson " + lessonId + " does not belong to course " + courseId);
        }

        if (!enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                student.getId(), courseId, EnrollmentStatus.ACTIVE)) {
            throw new AccessDeniedException("You must be enrolled in this course to track progress");
        }

        // Check if progress entry already exists
        boolean exists = progressRepository.findByStudentIdAndLessonId(student.getId(), lessonId).isPresent();
        if (!exists) {
            StudentProgress progress = StudentProgress.builder()
                    .student(student)
                    .lesson(lesson)
                    .completed(true)
                    .build();
            progressRepository.save(progress);
        }
    }

    /**
     * Calculates the overall completion percentage of a course for a student.
     */
    @Transactional(readOnly = true)
    public CourseProgressResponse getCourseProgress(Long courseId, User student) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

        // Fetch all modules and their respective lesson IDs for this course
        List<Module> modules = moduleRepository.findByCourseIdOrderByModuleOrderAsc(courseId);
        List<Long> allLessonIds = modules.stream()
                .flatMap(m -> lessonRepository.findByModuleIdOrderByLessonOrderAsc(m.getId()).stream())
                .map(Lesson::getId)
                .toList();

        long totalLessons = allLessonIds.size();
        if (totalLessons == 0) {
            return new CourseProgressResponse(courseId, course.getTitle(), 0, 0, 0.0);
        }

        // Count how many of those lessons the student has completed
        long completedLessons = progressRepository.countByStudentIdAndLessonIdIn(student.getId(), allLessonIds);
        double percentage = ((double) completedLessons / totalLessons) * 100.0;

        return CourseProgressResponse.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .totalLessons(totalLessons)
                .completedLessons(completedLessons)
                .completionPercentage(Math.round(percentage * 100.0) / 100.0) // Round to 2 decimal places
                .build();
    }
}