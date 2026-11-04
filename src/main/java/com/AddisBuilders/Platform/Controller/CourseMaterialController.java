package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.CreateMaterialRequest;
import com.AddisBuilders.Platform.DTO.MaterialResponse;
import com.AddisBuilders.Platform.DTO.UpdateMaterialRequest;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.CourseMaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Course learning-resource endpoints. All routes are relative to the
 * application context path, so externally they are {@code /api/courses/...}.
 */
@RestController
@RequestMapping("/courses/{courseId}/materials")
@RequiredArgsConstructor
public class CourseMaterialController {

    private final CourseMaterialService service;

    @GetMapping
    public ResponseEntity<List<MaterialResponse>> list(
            @PathVariable Long courseId,
            @AuthenticationPrincipal User viewer) {
        return ResponseEntity.ok(service.list(courseId, viewer));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<MaterialResponse> create(
            @PathVariable Long courseId,
            @Valid @RequestBody CreateMaterialRequest request,
            @AuthenticationPrincipal User actor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(courseId, request, actor));
    }

    @PatchMapping("/{materialId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<MaterialResponse> update(
            @PathVariable Long courseId,
            @PathVariable Long materialId,
            @Valid @RequestBody UpdateMaterialRequest request,
            @AuthenticationPrincipal User actor) {
        return ResponseEntity.ok(service.update(courseId, materialId, request, actor));
    }

    @DeleteMapping("/{materialId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<Void> delete(
            @PathVariable Long courseId,
            @PathVariable Long materialId,
            @AuthenticationPrincipal User actor) {
        service.delete(courseId, materialId, actor);
        return ResponseEntity.noContent().build();
    }
}
