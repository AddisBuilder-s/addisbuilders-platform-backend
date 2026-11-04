package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.CreateLessonRequest;
import com.AddisBuilders.Platform.DTO.CreateModuleRequest;
import com.AddisBuilders.Platform.DTO.LessonResponse;
import com.AddisBuilders.Platform.DTO.ModuleResponses;
import com.AddisBuilders.Platform.DTO.ReorderRequest;
import com.AddisBuilders.Platform.DTO.UpdateLessonRequest;
import com.AddisBuilders.Platform.DTO.UpdateModuleRequest;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.CurriculumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the nested course curriculum.
 *
 * <p>Module endpoints live under {@code /courses/{courseId}/modules}; lesson
 * endpoints live under {@code /modules/{moduleId}/lessons}. Spring Boot's
 * context path already supplies the {@code /api} prefix.</p>
 */
@RestController
@RequiredArgsConstructor
public class CurriculumController {

    private final CurriculumService curriculumService;

    @GetMapping("/courses/{courseId}/modules")
    public ResponseEntity<List<ModuleResponses>> getCourseCurriculum(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(curriculumService.getCourseCurriculum(courseId, currentUser));
    }

    /* ------------------------------------------------------------------ */
    /*                              Modules                               */
    /* ------------------------------------------------------------------ */

    @PostMapping("/courses/{courseId}/modules")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<ModuleResponses> addModule(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateModuleRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ModuleResponses created = curriculumService.addModuleToCourse(courseId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/courses/{courseId}/modules/reorder")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<List<ModuleResponses>> reorderModules(
            @PathVariable Long courseId,
            @Valid @RequestBody ReorderRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(curriculumService.reorderModules(courseId, request, currentUser));
    }

    @PatchMapping("/modules/{moduleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<ModuleResponses> updateModule(
            @PathVariable Long moduleId,
            @Valid @RequestBody UpdateModuleRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(curriculumService.updateModule(moduleId, request, currentUser));
    }

    @DeleteMapping("/modules/{moduleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<Void> deleteModule(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal User currentUser
    ) {
        curriculumService.deleteModule(moduleId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /* ------------------------------------------------------------------ */
    /*                              Lessons                               */
    /* ------------------------------------------------------------------ */

    @PostMapping("/modules/{moduleId}/lessons")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<LessonResponse> addLesson(
            @PathVariable Long moduleId,
            @Valid @RequestBody CreateLessonRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        LessonResponse created = curriculumService.addLessonToModule(moduleId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/modules/{moduleId}/lessons/reorder")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<List<LessonResponse>> reorderLessons(
            @PathVariable Long moduleId,
            @Valid @RequestBody ReorderRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(curriculumService.reorderLessons(moduleId, request, currentUser));
    }

    @PatchMapping("/lessons/{lessonId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody UpdateLessonRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(curriculumService.updateLesson(lessonId, request, currentUser));
    }

    @DeleteMapping("/lessons/{lessonId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal User currentUser
    ) {
        curriculumService.deleteLesson(lessonId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
