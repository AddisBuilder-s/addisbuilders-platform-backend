package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.CreateLessonRequest;
import com.AddisBuilders.Platform.DTO.CreateModuleRequest;
import com.AddisBuilders.Platform.DTO.LessonResponse;
import com.AddisBuilders.Platform.DTO.ModuleResponses;
import com.AddisBuilders.Platform.Model.*;
import com.AddisBuilders.Platform.Model.Module;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Repository.CourseRepository;
import com.AddisBuilders.Platform.Repository.LessonRepository;
import com.AddisBuilders.Platform.Repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service handling business logic for nested course modules and lessons.
 */
@Service
@RequiredArgsConstructor
public class CurriculumService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public ModuleResponses addModuleToCourse(Long courseId, CreateModuleRequest request, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));

        if (!course.getMentor().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Unauthorized: You do not own this course");
        }

        Module module = Module.builder()
                .course(course)
                .title(request.title())
                .moduleOrder(request.moduleOrder())
                .build();

        Module savedModule = moduleRepository.save(module);
        return mapModuleToResponse(savedModule);
    }

    @Transactional
    public LessonResponse addLessonToModule(Long moduleId, CreateLessonRequest request, User currentUser) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));

        Course course = module.getCourse();
        if (!course.getMentor().getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Unauthorized: You do not own this course");
        }

        Lesson lesson = Lesson.builder()
                .module(module)
                .title(request.title())
                .type(request.type())
                .body(request.body())
                .contentUrl(request.contentUrl())
                .lessonOrder(request.lessonOrder())
                .build();

        Lesson savedLesson = lessonRepository.save(lesson);
        return mapLessonToResponse(savedLesson);
    }

    @Transactional(readOnly = true)
    public List<ModuleResponses> getCourseCurriculum(Long courseId) {
        List<Module> modules = moduleRepository.findByCourseIdOrderByModuleOrderAsc(courseId);
        return modules.stream().map(this::mapModuleToResponse).toList();
    }

    private ModuleResponses mapModuleToResponse(Module module) {
        List<LessonResponse> lessonResponses = lessonRepository.findByModuleIdOrderByLessonOrderAsc(module.getId())
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
                .lessonOrder(lesson.getLessonOrder())
                .build();
    }
}