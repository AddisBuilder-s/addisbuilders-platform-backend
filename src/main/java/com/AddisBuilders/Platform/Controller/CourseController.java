package com.AddisBuilders.Platform.Controller;
import com.AddisBuilders.Platform.DTO.CourseResponse;
import com.AddisBuilders.Platform.DTO.CreateCourseRequest;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.CourseService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller providing HTTP API endpoints for managing and retrieving platform courses.
 */
@RestController
@RequestMapping("/api/courses")
@Getter // Explicitly ensures Lombok generates getters
@Setter // Explicitly ensures Lombok generates setters
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * Create a new course. Restricted to MENTOR and SUPER_ADMIN roles.
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('MENTOR', 'SUPER_ADMIN')")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(courseService.createCourse(request, currentUser));
    }

    /**
     * Public endpoint to retrieve all published courses for student discovery.
     */
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllPublishedCourses() {
        return ResponseEntity.ok(courseService.getAllPublishedCourses());
    }

    /**
     * Retrieve a specific course by its unique ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    /**
     * Publish a course. Restricted to the MENTOR who owns the course or a SUPER_ADMIN.
     */
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('MENTOR', 'SUPER_ADMIN')")
    public ResponseEntity<CourseResponse> publishCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(courseService.publishCourse(id, currentUser));
    }
}