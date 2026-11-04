package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.MaterialType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MaterialResponse(
        Long id,
        Long courseId,
        String title,
        String description,
        MaterialType type,
        String url,
        Integer order,
        LocalDateTime createdAt
) {}
