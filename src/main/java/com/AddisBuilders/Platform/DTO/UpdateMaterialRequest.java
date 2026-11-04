package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.MaterialType;
import jakarta.validation.constraints.Size;

public record UpdateMaterialRequest(
        @Size(max = 200, message = "Material title cannot exceed 200 characters")
        String title,
        String description,
        MaterialType type,
        @Size(max = 1000, message = "Material URL cannot exceed 1000 characters")
        String url,
        Integer order
) {}
