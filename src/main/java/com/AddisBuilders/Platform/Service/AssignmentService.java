package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.Exception.ResourceNotFoundException;
import com.AddisBuilders.Platform.DTO.AssignmentResponse;
import com.AddisBuilders.Platform.DTO.CreateAssignmentRequest;
import com.AddisBuilders.Platform.Model.Assignment;
import com.AddisBuilders.Platform.Model.Course;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Repository.AssignmentRepository;
import com.AddisBuilders.Platform.Repository.CourseRepository;
import com.AddisBuilders.Platform.Repository.EnrollmentRepository;
import com.AddisBuilders.Platform.Model.enums.EnrollmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing assignment creation, retrieval, and authorization checks.
 */
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuditLogService auditLog;

    /**
     * Creates a new assignment for a course. Restricted to the course owner mentor or super admin.
     */
    @Transactional
    public AssignmentResponse createAssignment(Long courseId, CreateAssignmentRequest request, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Verify authorization: only the course mentor or super admin can add assignments
        if (!course.getMentor().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("You do not own this course");
        }

        Assignment assignment = Assignment.builder()
                .course(course)
                .title(request.title())
                .instructions(request.description())
                .dueAt(request.dueDate())
                .points(request.maxPoints())
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);
        auditLog.record(currentUser, "ASSIGNMENT_CREATED", "ASSIGNMENT", savedAssignment.getId(), savedAssignment.getTitle(), null);
        return mapToResponse(savedAssignment);
    }

    /**
     * Retrieves all assignments linked to a specific course.
     */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByCourse(Long courseId, User viewer) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        assertCanView(course, viewer);

        return assignmentRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    /**
     * Retrieves all coursework assignments across all courses in the platform.
     */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAllAssignments(User viewer) {
        return assignmentRepository.findAll().stream()
                .filter(assignment -> canView(assignment.getCourse(), viewer))
                .map(this::mapToResponse)
                .toList();
    }

    private void assertCanView(Course course, User viewer) {
        if (!canView(course, viewer)) throw new AccessDeniedException("You must be enrolled in this course to view assignments");
    }

    private boolean canView(Course course, User viewer) {
        if (viewer == null) return false;
        if (viewer.getRole() == UserRole.SUPER_ADMIN || course.getMentor().getId().equals(viewer.getId())) return true;
        return viewer.getRole() == UserRole.LEARNER && enrollmentRepository.existsByStudentIdAndCourseIdAndStatusIn(
                viewer.getId(), course.getId(), List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED));
    }

    /**
     * Helper mapper to convert Assignment entity to response DTO.
     */
    private AssignmentResponse mapToResponse(Assignment assignment) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .courseId(assignment.getCourse().getId())
                .courseTitle(assignment.getCourse().getTitle())
                .title(assignment.getTitle())
                .description(assignment.getInstructions())
                .dueDate(assignment.getDueAt())
                .maxPoints(assignment.getPoints())
                .build();
    }
}
