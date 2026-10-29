package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.*;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.CurriculumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller handling API endpoints for modules and lessons curriculum hierarchy.
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CurriculumController {

    private final CurriculumService curriculumService;

    @PostMapping("/{courseId}/modules")
    @PreAuthorize("hasAnyRole('MENTOR', 'SUPER_ADMIN')")
    public ResponseEntity<ModuleResponses> addModule(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateModuleRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(curriculumService.addModuleToCourse(courseId, request, currentUser));
    }

    @PostMapping("/modules/{moduleId}/lessons")
    @PreAuthorize("hasAnyRole('MENTOR', 'SUPER_ADMIN')")
    public ResponseEntity<LessonResponse> addLesson(
            @PathVariable Long moduleId,
            @Valid @RequestBody CreateLessonRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(curriculumService.addLessonToModule(moduleId, request, currentUser));
    }

    @GetMapping("/{courseId}/curriculum")
    public ResponseEntity<List<ModuleResponses>> getCourseCurriculum(@PathVariable Long courseId) {
        return ResponseEntity.ok(curriculumService.getCourseCurriculum(courseId));
    }
}