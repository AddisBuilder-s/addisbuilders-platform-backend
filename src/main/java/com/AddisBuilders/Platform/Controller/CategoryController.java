package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.CategoryResponse;
import com.AddisBuilders.Platform.DTO.CreateCategoryRequest;
import com.AddisBuilders.Platform.Exception.ResourceNotFoundException;
import com.AddisBuilders.Platform.Model.CourseCategory;
import com.AddisBuilders.Platform.Repository.CourseCategoryRepository;
import com.AddisBuilders.Platform.Service.AuditLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public catalogue categories. Exposed both as {@code /course-categories}
 * (canonical) and {@code /categories} (convenience alias).
 */
@RestController
@RequestMapping({"/course-categories", "/categories"})
@RequiredArgsConstructor
public class CategoryController {

    private final CourseCategoryRepository repository;
    private final AuditLogService auditLog;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(repository.findAll().stream().map(this::map).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(repository.findById(id)
                .map(this::map)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        CourseCategory category = CourseCategory.builder()
                .name(request.name().trim())
                .description(request.description())
                .build();
        CourseCategory saved = repository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(map(saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        CourseCategory category = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        repository.delete(category);
        return ResponseEntity.noContent().build();
    }

    private CategoryResponse map(CourseCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
