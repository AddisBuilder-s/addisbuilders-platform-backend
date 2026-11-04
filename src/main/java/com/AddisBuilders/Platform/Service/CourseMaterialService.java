package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.CreateMaterialRequest;
import com.AddisBuilders.Platform.DTO.MaterialResponse;
import com.AddisBuilders.Platform.DTO.UpdateMaterialRequest;
import com.AddisBuilders.Platform.Exception.ResourceNotFoundException;
import com.AddisBuilders.Platform.Model.Course;
import com.AddisBuilders.Platform.Model.CourseMaterial;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.CourseStatus;
import com.AddisBuilders.Platform.Model.enums.MaterialType;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Repository.CourseMaterialRepository;
import com.AddisBuilders.Platform.Repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages supplemental resources attached to a course. Reads follow the same
 * visibility rules as the course itself (published for everyone, drafts only
 * for the owning mentor/admin). Writes are restricted to the course owner or a
 * super admin.
 */
@Service
@RequiredArgsConstructor
public class CourseMaterialService {

    private final CourseRepository courseRepository;
    private final CourseMaterialRepository materialRepository;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public List<MaterialResponse> list(Long courseId, User viewer) {
        Course course = requireCourse(courseId);
        assertCanView(course, viewer);
        return materialRepository.findByCourseIdOrderBySortOrderAscIdAsc(courseId)
                .stream()
                .map(CourseService::mapMaterial)
                .toList();
    }

    @Transactional
    public MaterialResponse create(Long courseId, CreateMaterialRequest request, User actor) {
        Course course = requireOwnedCourse(courseId, actor);

        int nextOrder = request.order() != null
                ? request.order()
                : (int) materialRepository.findByCourseIdOrderBySortOrderAscIdAsc(courseId).size() + 1;

        CourseMaterial material = CourseMaterial.builder()
                .course(course)
                .title(request.title().trim())
                .description(request.description())
                .type(request.type() != null ? request.type() : MaterialType.LINK)
                .url(request.url())
                .sortOrder(nextOrder)
                .build();

        CourseMaterial saved = materialRepository.save(material);
        auditLog.record(actor, "MATERIAL_CREATED", "COURSE_MATERIAL", saved.getId(), saved.getTitle(), null);
        return CourseService.mapMaterial(saved);
    }

    @Transactional
    public MaterialResponse update(Long courseId, Long materialId, UpdateMaterialRequest request, User actor) {
        requireOwnedCourse(courseId, actor);
        CourseMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Course material not found with ID: " + materialId));
        if (!material.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Course material " + materialId + " does not belong to course " + courseId);
        }

        if (request.title() != null) {
            String title = request.title().trim();
            if (title.isEmpty()) {
                throw new IllegalArgumentException("Material title cannot be blank");
            }
            material.setTitle(title);
        }
        if (request.description() != null) material.setDescription(request.description());
        if (request.type() != null) material.setType(request.type());
        if (request.url() != null) material.setUrl(request.url());
        if (request.order() != null) material.setSortOrder(request.order());

        CourseMaterial saved = materialRepository.save(material);
        auditLog.record(actor, "MATERIAL_UPDATED", "COURSE_MATERIAL", saved.getId(), saved.getTitle(), null);
        return CourseService.mapMaterial(saved);
    }

    @Transactional
    public void delete(Long courseId, Long materialId, User actor) {
        requireOwnedCourse(courseId, actor);
        CourseMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Course material not found with ID: " + materialId));
        if (!material.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Course material " + materialId + " does not belong to course " + courseId);
        }
        auditLog.record(actor, "MATERIAL_DELETED", "COURSE_MATERIAL", materialId, material.getTitle(), null);
        materialRepository.delete(material);
    }

    private Course requireCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
    }

    private Course requireOwnedCourse(Long courseId, User actor) {
        Course course = requireCourse(courseId);
        boolean owner = actor != null && course.getMentor() != null
                && course.getMentor().getId().equals(actor.getId());
        if (!owner && (actor == null || actor.getRole() != UserRole.SUPER_ADMIN)) {
            throw new AccessDeniedException("You do not own this course");
        }
        return course;
    }

    private void assertCanView(Course course, User viewer) {
        boolean ownerOrAdmin = viewer != null
                && (course.getMentor().getId().equals(viewer.getId())
                    || viewer.getRole() == UserRole.SUPER_ADMIN);
        if (course.getStatus() != CourseStatus.PUBLISHED && !ownerOrAdmin) {
            throw new AccessDeniedException("This course is not published yet");
        }
    }
}
