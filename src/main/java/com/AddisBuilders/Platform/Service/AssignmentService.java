package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.Exception.ResourceNotFoundException;
import com.AddisBuilders.Platform.DTO.AssignmentResponse;
import com.AddisBuilders.Platform.DTO.CreateAssignmentRequest;
import com.AddisBuilders.Platform.DTO.UpdateAssignmentRequest;
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
                .attachmentUrl(request.attachmentUrl())
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

    /**
     * Retrieves a single assignment and checks the caller can view its course.
     */
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignment(Long assignmentId, User viewer) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + assignmentId));
        assertCanView(assignment.getCourse(), viewer);
        return mapToResponse(assignment);
    }

    /**
     * Partially updates an assignment. Only the owning mentor or a super admin
     * may edit it.
     */
    @Transactional
    public AssignmentResponse updateAssignment(Long assignmentId, UpdateAssignmentRequest request, User currentUser) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + assignmentId));

        Course course = assignment.getCourse();
        if (!course.getMentor().getId().equals(currentUser.getId())
                && currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("You do not own the course for this assignment");
        }

        if (request.title() != null) {
            String title = request.title().trim();
            if (title.isEmpty()) {
                throw new IllegalArgumentException("Assignment title cannot be blank");
            }
            assignment.setTitle(title);
        }
        if (request.description() != null) assignment.setInstructions(request.description());
        if (request.dueDate() != null) assignment.setDueAt(request.dueDate());
        if (request.maxPoints() != null) assignment.setPoints(request.maxPoints());
        if (request.attachmentUrl() != null) assignment.setAttachmentUrl(request.attachmentUrl());

        Assignment saved = assignmentRepository.save(assignment);
        auditLog.record(currentUser, "ASSIGNMENT_UPDATED", "ASSIGNMENT", saved.getId(), saved.getTitle(), null);
        return mapToResponse(saved);
    }

    /**
     * Deletes an assignment and (through the database cascade) its submissions.
     */
    @Transactional
    public void deleteAssignment(Long assignmentId, User currentUser) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with ID: " + assignmentId));

        Course course = assignment.getCourse();
        if (!course.getMentor().getId().equals(currentUser.getId())
                && currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("You do not own the course for this assignment");
        }

        auditLog.record(currentUser, "ASSIGNMENT_DELETED", "ASSIGNMENT", assignmentId, assignment.getTitle(), null);
        assignmentRepository.delete(assignment);
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
                .attachmentUrl(assignment.getAttachmentUrl())
                .createdAt(assignment.getCreatedAt())
                .build();
    }
}
