package com.AddisBuilders.Platform.Controller;
import com.AddisBuilders.Platform.DTO.CourseResponse;
import com.AddisBuilders.Platform.DTO.CreateCourseRequest;
import com.AddisBuilders.Platform.DTO.UpdateCourseRequest;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.CourseService;
import com.AddisBuilders.Platform.Service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * REST Controller providing HTTP API endpoints for managing and retrieving platform courses.
 */
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    /**
     * Create a new course. Restricted to MENTOR and SUPER_ADMIN roles.
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        CourseResponse created = courseService.createCourse(request, currentUser);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri()).body(created);
    }

    /**
     * Public endpoint to retrieve all published courses for student discovery.
     */
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getCourses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(courseService.getAllCourses(currentUser, category, level, search));
    }



    /**
     * Retrieve a specific course by its unique ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(courseService.getCourseById(id, currentUser));
    }

    /**
     * Publish a course. Restricted to the MENTOR who owns the course or a SUPER_ADMIN.
     */
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<CourseResponse> publishCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(courseService.publishCourse(id, currentUser));
    }
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(courseService.updateCourse(id, request, currentUser));
    }

    @PostMapping("/{courseId}/enroll")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<CourseResponse> enrollCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(enrollmentService.enrollStudent(courseId, currentUser));
    }
}
