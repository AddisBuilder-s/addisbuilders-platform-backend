package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.CourseProgressResponse;
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
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    /**
     * Mark a lesson as completed. Restricted to STUDENT role.
     */
    @PostMapping("/lessons/{lessonId}/complete")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<String> markLessonComplete(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal User currentUser
    ) {
       boolean isCompleted = progressService.markLessonComplete(lessonId, currentUser);
       if (!isCompleted){
           return ResponseEntity.ok("Lesson marked as completed successfully.");
       }else {
           return  new ResponseEntity<>( "Lesson Already marked as complete",HttpStatus.BAD_REQUEST);
       }

    }

    /**
     * Get course progress percentage for the authenticated student.
     */
    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(progressService.getCourseProgress(courseId, currentUser));
    }
}
