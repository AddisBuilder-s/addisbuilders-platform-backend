package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.Exception.ResourceNotFoundException;

import com.AddisBuilders.Platform.DTO.CreateLessonRequest;
import com.AddisBuilders.Platform.DTO.CreateModuleRequest;
import com.AddisBuilders.Platform.DTO.LessonResponse;
import com.AddisBuilders.Platform.DTO.ModuleResponses;
import com.AddisBuilders.Platform.DTO.ReorderRequest;
import com.AddisBuilders.Platform.DTO.UpdateLessonRequest;
import com.AddisBuilders.Platform.DTO.UpdateModuleRequest;
import com.AddisBuilders.Platform.Model.Course;
import com.AddisBuilders.Platform.Model.Lesson;
import com.AddisBuilders.Platform.Model.Module;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.CourseStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Repository.CourseRepository;
import com.AddisBuilders.Platform.Repository.LessonRepository;
import com.AddisBuilders.Platform.Repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service handling business logic for the nested course module / lesson
 * curriculum. Mutations verify that the authenticated mentor owns the parent
 * course (or is a super admin) before touching any row.
 */
@Service
@RequiredArgsConstructor
public class CurriculumService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final AuditLogService auditLog;

    /* ------------------------------------------------------------------ */
    /*                              Modules                               */
    /* ------------------------------------------------------------------ */

    @Transactional
    public ModuleResponses addModuleToCourse(Long courseId, CreateModuleRequest request, User currentUser) {
        Course course = requireOwnedCourse(courseId, currentUser);

        if (moduleRepository.existsByCourseIdAndModuleOrder(courseId, request.moduleOrder())) {
            throw new IllegalArgumentException(
                    "A module already exists at position " + request.moduleOrder() + ". Choose another position.");
        }

        Module module = Module.builder()
                .course(course)
                .title(request.title().trim())
                .moduleOrder(request.moduleOrder())
                .build();

        Module saved = moduleRepository.save(module);
        auditLog.record(currentUser, "MODULE_CREATED", "MODULE", saved.getId(), saved.getTitle(), null);
        return mapModuleToResponse(saved);
    }

    @Transactional
    public ModuleResponses updateModule(Long moduleId, UpdateModuleRequest request, User currentUser) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + moduleId));
        return updateModule(module.getCourse().getId(), moduleId, request, currentUser);
    }

    @Transactional
    public ModuleResponses updateModule(Long courseId, Long moduleId, UpdateModuleRequest request, User currentUser) {
        Course course = requireOwnedCourse(courseId, currentUser);
        Module module = moduleRepository.findByIdAndCourseId(moduleId, course.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + moduleId));

        if (request.title() != null) {
            String title = request.title().trim();
            if (title.isEmpty()) {
                throw new IllegalArgumentException("Module title cannot be blank");
            }
            module.setTitle(title);
        }

        if (request.moduleOrder() != null && !request.moduleOrder().equals(module.getModuleOrder())) {
            if (moduleRepository.existsByCourseIdAndModuleOrderAndIdNot(
                    course.getId(), request.moduleOrder(), module.getId())) {
                throw new IllegalArgumentException(
                        "A module already exists at position " + request.moduleOrder() + ". Choose another position.");
            }
            module.setModuleOrder(request.moduleOrder());
        }

        Module saved = moduleRepository.save(module);
        auditLog.record(currentUser, "MODULE_UPDATED", "MODULE", saved.getId(), saved.getTitle(), null);
        return mapModuleToResponse(saved);
    }

    @Transactional
    public void deleteModule(Long moduleId, User currentUser) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + moduleId));
        deleteModule(module.getCourse().getId(), moduleId, currentUser);
    }

    @Transactional
    public void deleteModule(Long courseId, Long moduleId, User currentUser) {
        Course course = requireOwnedCourse(courseId, currentUser);
        Module module = moduleRepository.findByIdAndCourseId(moduleId, course.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + moduleId));

        auditLog.record(currentUser, "MODULE_DELETED", "MODULE", moduleId, module.getTitle(), null);
        moduleRepository.delete(module);
        moduleRepository.flush();
        resequenceModules(course.getId());
    }

    @Transactional
    public List<ModuleResponses> reorderModules(Long courseId, ReorderRequest request, User currentUser) {
        Course course = requireOwnedCourse(courseId, currentUser);
        List<Module> modules = moduleRepository.findByCourseIdOrderByModuleOrderAsc(course.getId());

        assertCompleteSet(
                request.ids(),
                modules.stream().map(Module::getId).toList(),
                "module");

        // Move every module to a temporary, collision-free order before
        // applying the final sequence. This keeps the unique
        // (course_id, module_order) index satisfied at every flush.
        for (int index = 0; index < request.ids().size(); index++) {
            Module module = findModule(modules, request.ids().get(index));
            module.setModuleOrder(-(index + 1));
        }
        moduleRepository.flush();

        for (int index = 0; index < request.ids().size(); index++) {
            Module module = findModule(modules, request.ids().get(index));
            module.setModuleOrder(index + 1);
        }
        moduleRepository.flush();

        auditLog.record(currentUser, "MODULES_REORDERED", "COURSE", course.getId(), "Module order updated", null);
        return moduleRepository.findByCourseIdOrderByModuleOrderAsc(course.getId())
                .stream()
                .map(this::mapModuleToResponse)
                .toList();
    }

    /* ------------------------------------------------------------------ */
    /*                              Lessons                               */
    /* ------------------------------------------------------------------ */

    @Transactional
    public LessonResponse addLessonToModule(Long moduleId, CreateLessonRequest request, User currentUser) {
        Module module = requireOwnedModule(moduleId, currentUser);

        if (lessonRepository.existsByModuleIdAndLessonOrder(moduleId, request.lessonOrder())) {
            throw new IllegalArgumentException(
                    "A lesson already exists at position " + request.lessonOrder() + " in this module.");
        }

        Lesson lesson = Lesson.builder()
                .module(module)
                .title(request.title().trim())
                .type(request.type())
                .body(request.body())
                .contentUrl(request.contentUrl())
                .durationMinutes(request.durationMinutes())
                .lessonOrder(request.lessonOrder())
                .build();

        Lesson saved = lessonRepository.save(lesson);
        auditLog.record(currentUser, "LESSON_CREATED", "LESSON", saved.getId(), saved.getTitle(), null);
        return mapLessonToResponse(saved);
    }

    @Transactional
    public LessonResponse updateLesson(Long lessonId, UpdateLessonRequest request, User currentUser) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));
        return updateLesson(lesson.getModule().getId(), lessonId, request, currentUser);
    }

    @Transactional
    public LessonResponse updateLesson(Long moduleId, Long lessonId, UpdateLessonRequest request, User currentUser) {
        Module module = requireOwnedModule(moduleId, currentUser);
        Lesson lesson = lessonRepository.findByIdAndModuleId(lessonId, module.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));

        if (request.title() != null) {
            String title = request.title().trim();
            if (title.isEmpty()) {
                throw new IllegalArgumentException("Lesson title cannot be blank");
            }
            lesson.setTitle(title);
        }
        if (request.type() != null) {
            lesson.setType(request.type());
        }
        if (request.body() != null) {
            lesson.setBody(request.body());
        }
        if (request.contentUrl() != null) {
            lesson.setContentUrl(request.contentUrl());
        }
        if (request.durationMinutes() != null) {
            lesson.setDurationMinutes(request.durationMinutes());
        }
        if (request.lessonOrder() != null && !request.lessonOrder().equals(lesson.getLessonOrder())) {
            if (lessonRepository.existsByModuleIdAndLessonOrderAndIdNot(
                    module.getId(), request.lessonOrder(), lesson.getId())) {
                throw new IllegalArgumentException(
                        "A lesson already exists at position " + request.lessonOrder() + " in this module.");
            }
            lesson.setLessonOrder(request.lessonOrder());
        }

        Lesson saved = lessonRepository.save(lesson);
        auditLog.record(currentUser, "LESSON_UPDATED", "LESSON", saved.getId(), saved.getTitle(), null);
        return mapLessonToResponse(saved);
    }

    @Transactional
    public void deleteLesson(Long lessonId, User currentUser) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));
        deleteLesson(lesson.getModule().getId(), lessonId, currentUser);
    }

    @Transactional
    public void deleteLesson(Long moduleId, Long lessonId, User currentUser) {
        Module module = requireOwnedModule(moduleId, currentUser);
        Lesson lesson = lessonRepository.findByIdAndModuleId(lessonId, module.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + lessonId));

        auditLog.record(currentUser, "LESSON_DELETED", "LESSON", lessonId, lesson.getTitle(), null);
        lessonRepository.delete(lesson);
        lessonRepository.flush();
        resequenceLessons(module.getId());
    }

    @Transactional
    public List<LessonResponse> reorderLessons(Long moduleId, ReorderRequest request, User currentUser) {
        Module module = requireOwnedModule(moduleId, currentUser);
        List<Lesson> lessons = lessonRepository.findByModuleIdOrderByLessonOrderAsc(module.getId());

        assertCompleteSet(
                request.ids(),
                lessons.stream().map(Lesson::getId).toList(),
                "lesson");

        for (int index = 0; index < request.ids().size(); index++) {
            Lesson lesson = findLesson(lessons, request.ids().get(index));
            lesson.setLessonOrder(-(index + 1));
        }
        lessonRepository.flush();

        for (int index = 0; index < request.ids().size(); index++) {
            Lesson lesson = findLesson(lessons, request.ids().get(index));
            lesson.setLessonOrder(index + 1);
        }
        lessonRepository.flush();

        auditLog.record(currentUser, "LESSONS_REORDERED", "MODULE", module.getId(), "Lesson order updated", null);
        return lessonRepository.findByModuleIdOrderByLessonOrderAsc(module.getId())
                .stream()
                .map(this::mapLessonToResponse)
                .toList();
    }

    /* ------------------------------------------------------------------ */
    /*                              Reads                                 */
    /* ------------------------------------------------------------------ */

    @Transactional(readOnly = true)
    public List<ModuleResponses> getCourseCurriculum(Long courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        boolean isOwnerOrAdmin = currentUser != null
                && (course.getMentor().getId().equals(currentUser.getId())
                    || currentUser.getRole() == UserRole.SUPER_ADMIN);

        if (course.getStatus() != CourseStatus.PUBLISHED && !isOwnerOrAdmin) {
            throw new AccessDeniedException("This course is not published yet");
        }

        return moduleRepository.findByCourseIdOrderByModuleOrderAsc(courseId)
                .stream()
                .map(this::mapModuleToResponse)
                .toList();
    }

    /* ------------------------------------------------------------------ */
    /*                             Helpers                                */
    /* ------------------------------------------------------------------ */

    private Course requireOwnedCourse(Long courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        assertOwnership(course, currentUser);
        return course;
    }

    private Module requireOwnedModule(Long moduleId, User currentUser) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + moduleId));

        assertOwnership(module.getCourse(), currentUser);
        return module;
    }

    private void assertOwnership(Course course, User currentUser) {
        boolean isOwner = currentUser != null
                && course.getMentor() != null
                && course.getMentor().getId().equals(currentUser.getId());

        if (!isOwner && (currentUser == null || currentUser.getRole() != UserRole.SUPER_ADMIN)) {
            throw new AccessDeniedException("You do not own this course");
        }
    }

    private void assertCompleteSet(List<Long> requested, List<Long> existing, String label) {
        Set<Long> requestedSet = new HashSet<>(requested);
        if (requestedSet.size() != requested.size()) {
            throw new IllegalArgumentException("The reorder list contains duplicate " + label + " ids");
        }
        if (!requestedSet.equals(new HashSet<>(existing))) {
            throw new IllegalArgumentException(
                    "The reorder list must contain every " + label + " in this curriculum exactly once");
        }
    }

    private Module findModule(List<Module> modules, Long id) {
        return modules.stream()
                .filter(module -> module.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Module not found with ID: " + id));
    }

    private Lesson findLesson(List<Lesson> lessons, Long id) {
        return lessons.stream()
                .filter(lesson -> lesson.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found with ID: " + id));
    }

    /**
     * Collapses gaps left behind by a deletion so the remaining modules stay at
     * 1..n. Uses a temporary offset so the unique order index never clashes.
     */
    private void resequenceModules(Long courseId) {
        List<Module> modules = moduleRepository.findByCourseIdOrderByModuleOrderAsc(courseId);
        boolean needsResequence = false;
        for (int index = 0; index < modules.size(); index++) {
            if (!modules.get(index).getModuleOrder().equals(index + 1)) {
                needsResequence = true;
                break;
            }
        }
        if (!needsResequence) {
            return;
        }

        for (int index = 0; index < modules.size(); index++) {
            modules.get(index).setModuleOrder(-(index + 1));
        }
        moduleRepository.flush();

        for (int index = 0; index < modules.size(); index++) {
            modules.get(index).setModuleOrder(index + 1);
        }
        moduleRepository.flush();
    }

    private void resequenceLessons(Long moduleId) {
        List<Lesson> lessons = lessonRepository.findByModuleIdOrderByLessonOrderAsc(moduleId);
        boolean needsResequence = false;
        for (int index = 0; index < lessons.size(); index++) {
            if (!lessons.get(index).getLessonOrder().equals(index + 1)) {
                needsResequence = true;
                break;
            }
        }
        if (!needsResequence) {
            return;
        }

        for (int index = 0; index < lessons.size(); index++) {
            lessons.get(index).setLessonOrder(-(index + 1));
        }
        lessonRepository.flush();

        for (int index = 0; index < lessons.size(); index++) {
            lessons.get(index).setLessonOrder(index + 1);
        }
        lessonRepository.flush();
    }

    private ModuleResponses mapModuleToResponse(Module module) {
        List<LessonResponse> lessonResponses = lessonRepository
                .findByModuleIdOrderByLessonOrderAsc(module.getId())
                .stream()
                .map(this::mapLessonToResponse)
                .toList();

        return ModuleResponses.builder()
                .id(module.getId())
                .courseId(module.getCourse().getId())
                .title(module.getTitle())
                .moduleOrder(module.getModuleOrder())
                .lessons(lessonResponses)
                .build();
    }

    private LessonResponse mapLessonToResponse(Lesson lesson) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .moduleId(lesson.getModule().getId())
                .title(lesson.getTitle())
                .type(lesson.getType())
                .body(lesson.getBody())
                .contentUrl(lesson.getContentUrl())
                .durationMinutes(lesson.getDurationMinutes())
                .lessonOrder(lesson.getLessonOrder())
                .build();
    }
}
