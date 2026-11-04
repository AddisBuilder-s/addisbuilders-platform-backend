package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.CourseProgressResponse;
import com.AddisBuilders.Platform.DTO.LessonCompletionResponse;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling student learning progress and completion tracking endpoints.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    /**
     * Mark a lesson as completed. Restricted to STUDENT role.
     */
    @PostMapping({"/progress/courses/{courseId}/lessons/{lessonId}/complete",
            "/courses/{courseId}/lessons/{lessonId}/complete"})
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<LessonCompletionResponse> markLessonComplete(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal User currentUser
    ) {
        progressService.markLessonComplete(courseId, lessonId, currentUser);
        CourseProgressResponse progress = progressService.getCourseProgress(courseId, currentUser);

        return ResponseEntity.ok(new LessonCompletionResponse(
                progress.completionPercentage(),
                progress.completedLessons()
        ));
    }


    /**
     * Get course progress percentage for the authenticated student.
     */
    @GetMapping({"/progress/courses/{courseId}", "/courses/{courseId}/progress"})
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(progressService.getCourseProgress(courseId, currentUser));
    }
}
