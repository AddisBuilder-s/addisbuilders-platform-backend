package com.AddisBuilders.Platform.Service;
import com.AddisBuilders.Platform.DTO.AssignmentResponse;
import com.AddisBuilders.Platform.DTO.CreateAssignmentRequest;
import com.AddisBuilders.Platform.Model.Assignment;
import com.AddisBuilders.Platform.Model.Course;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Repository.AssignmentRepository;
import com.AddisBuilders.Platform.Repository.CourseRepository;
import lombok.RequiredArgsConstructor;
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

    /**
     * Creates a new assignment for a course. Restricted to the course owner mentor or super admin.
     */
    @Transactional
    public AssignmentResponse createAssignment(Long courseId, CreateAssignmentRequest request, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

        // Verify authorization: only the course mentor or super admin can add assignments
        if (!course.getMentor().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Unauthorized: You do not own this course");
        }

        Assignment assignment = Assignment.builder()
                .course(course)
                .title(request.title())
                .instructions(request.description())
                .dueAt(request.dueDate())
                .points(request.maxPoints())
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);
        return mapToResponse(savedAssignment);
    }

    /**
     * Retrieves all assignments linked to a specific course.
     */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByCourse(Long courseId) {
        // Ensure course exists
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("Course not found with ID: " + courseId);
        }

        return assignmentRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    /**
     * Retrieves all coursework assignments across all courses in the platform.
     */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
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