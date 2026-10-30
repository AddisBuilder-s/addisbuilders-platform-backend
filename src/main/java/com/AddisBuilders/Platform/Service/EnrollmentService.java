package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.CourseResponse;
import com.AddisBuilders.Platform.DTO.EnrollmentResponse;
import com.AddisBuilders.Platform.Model.Course;
import com.AddisBuilders.Platform.Model.Enrollment;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.CourseStatus;
import com.AddisBuilders.Platform.Model.enums.EnrollmentStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Repository.CourseRepository;
import com.AddisBuilders.Platform.Repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    /**
     * Enrolls a student in a course. If already enrolled, returns the existing enrollment/course.
     */
    @Transactional
    public CourseResponse enrollStudent(Long courseId, User student) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalArgumentException("You can only enroll in published courses.");
        }

        if (!enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            Enrollment enrollment = Enrollment.builder()
                    .student(student)
                    .course(course)
                    .status(EnrollmentStatus.ACTIVE)
                    .build();
            enrollmentRepository.save(enrollment);
        }

        return mapToCourseResponse(course);
    }

    /**
     * Retrieves all enrollments for a specific student.
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToEnrollmentResponse)
                .toList();
    }

    /**
     * Retrieves students enrolled under a specific mentor's courses.
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getInstructorStudents(Long mentorId) {
        return enrollmentRepository.findByCourseMentorId(mentorId)
                .stream()
                .map(this::mapToEnrollmentResponse)
                .toList();
    }

    /**
     * Updates enrollment status (e.g., ACTIVE -> COMPLETED or DROPPED).
     */
    @Transactional
    public EnrollmentResponse updateEnrollmentStatus(Long enrollmentId, EnrollmentStatus status, User currentUser) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment record not found with ID: " + enrollmentId));

        boolean ownsCourse = enrollment.getCourse().getMentor().getId().equals(currentUser.getId());
        if (!ownsCourse && currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("You do not have permission to update this enrollment");
        }

        enrollment.setStatus(status);
        Enrollment updated = enrollmentRepository.save(enrollment);
        return mapToEnrollmentResponse(updated);
    }

    private EnrollmentResponse mapToEnrollmentResponse(Enrollment e) {
        return EnrollmentResponse.builder()
                .id(e.getId())
                .studentId(e.getStudent().getId())
                .studentName(e.getStudent().getFullName())
                .studentEmail(e.getStudent().getEmail())
                .courseId(e.getCourse().getId())
                .courseTitle(e.getCourse().getTitle())
                .instructorId(String.valueOf(e.getCourse().getMentor().getId()))
                .status(e.getStatus())
                .enrolledAt(e.getEnrolledAt())
                .build();
    }

    private CourseResponse mapToCourseResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .level(course.getLevel())
                .status(course.getStatus())
                .thumbnailUrl(course.getThumbnailUrl())
                .mentorId(course.getMentor().getId())
                .instructorName(course.getMentor().getFullName()) // Must match field name instructorName
                .categoryId(course.getCategory() != null ? course.getCategory().getId() : null)
                .categoryName(course.getCategory() != null ? course.getCategory().getName() : null)
                .createdAt(course.getCreatedAt())
                .build();
    }
}