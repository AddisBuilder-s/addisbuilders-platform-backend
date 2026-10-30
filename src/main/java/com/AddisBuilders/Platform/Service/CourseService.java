package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.CourseResponse;
import com.AddisBuilders.Platform.DTO.CreateCourseRequest;
import com.AddisBuilders.Platform.DTO.UpdateCourseRequest;
import com.AddisBuilders.Platform.Model.*;
import com.AddisBuilders.Platform.Model.enums.CourseStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Repository.CourseCategoryRepository;
import com.AddisBuilders.Platform.Repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class handling all business logic operations for Course lifecycle management,
 * category linking, status transitions, and authorization checks.
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseCategoryRepository categoryRepository;

    /**
     * Creates a new course authored by the authenticated mentor.
     */
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request, User mentor) {
        // Find optional category if ID is provided
        CourseCategory category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + request.categoryId()));
        }

        // Build entity with default status DRAFT
        Course course = Course.builder()
                .title(request.title())
                .description(request.description())
                .level(request.level())
                .status(CourseStatus.DRAFT)
                .thumbnailUrl(request.thumbnailUrl())
                .mentor(mentor)
                .category(category)
                .build();

        Course savedCourse = courseRepository.save(course);
        return mapToResponse(savedCourse);
    }

    /**
     * Retrieves all published courses for student browsing.
     */
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses(User currentUser, String category, String level, String search) {
        List<Course> courses;

        // If mentor or admin, allow viewing DRAFT courses as well as PUBLISHED ones
        if (currentUser != null && (currentUser.getRole() == UserRole.MENTOR || currentUser.getRole() == UserRole.SUPER_ADMIN)) {
            courses = courseRepository.findAll();
        } else {
            // Learners and public visitors only see published courses
            courses = courseRepository.findByStatus(CourseStatus.PUBLISHED);
        }

        return courses.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves a single course by ID.
     */
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + id));
        return mapToResponse(course);
    }


    /**
     * Publishes a draft course, making it visible to students.
     */
    @Transactional
    public CourseResponse publishCourse(Long id, User mentor) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + id));

        // Ensure only the course author or super admin can update the course status
        if (!course.getMentor().getId().equals(mentor.getId()) && mentor.getRole() != UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Unauthorized: You do not own this course");
        }

        course.setStatus(CourseStatus.PUBLISHED);
        Course updatedCourse = courseRepository.save(course);
        return mapToResponse(updatedCourse);
    }
    /**
     * Updates an existing course authored by the mentor or super admin.
     */
    @Transactional
    public CourseResponse updateCourse(Long id, UpdateCourseRequest request, User mentor) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + id));

        if (!course.getMentor().getId().equals(mentor.getId()) && mentor.getRole() != UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Unauthorized: You do not own this course");
        }

        if (request.title() != null) course.setTitle(request.title());
        if (request.description() != null) course.setDescription(request.description());
        if (request.shortDescription() != null) course.setShortDescription(request.shortDescription());
        if (request.level() != null) course.setLevel(request.level());
        if (request.durationWeeks() != null) course.setDurationWeeks(request.durationWeeks());
        if (request.learningObjectives() != null) course.setLearningObjectives(request.learningObjectives());
        if (request.thumbnailUrl() != null) course.setThumbnailUrl(request.thumbnailUrl());
        if (request.status() != null) course.setStatus(request.status());
        if (request.categoryId() != null) {
            CourseCategory category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + request.categoryId()));
            course.setCategory(category);
        }

        Course updatedCourse = courseRepository.save(course);
        return mapToResponse(updatedCourse);
    }
    /**
     * Helper mapper to convert JPA entity to immutable DTO response.
     */
    private CourseResponse mapToResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .shortDescription(course.getShortDescription())
                .level(course.getLevel())
                .status(course.getStatus())
                .thumbnailUrl(course.getThumbnailUrl())
                .durationWeeks(course.getDurationWeeks())
                .learningObjectives(course.getLearningObjectives())
                .mentorId(course.getMentor().getId())
                .instructorName(course.getMentor().getFullName()) // Populates instructorName so card displays properly!
                .categoryId(course.getCategory() != null ? course.getCategory().getId() : null)
                .categoryName(course.getCategory() != null ? course.getCategory().getName() : null)
                .createdAt(course.getCreatedAt())
                .build();
    }
}