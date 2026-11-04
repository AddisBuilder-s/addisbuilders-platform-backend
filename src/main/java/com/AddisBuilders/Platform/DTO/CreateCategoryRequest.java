package com.AddisBuilders.Platform.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name cannot exceed 100 characters")
        String name,
        String description
) {}
