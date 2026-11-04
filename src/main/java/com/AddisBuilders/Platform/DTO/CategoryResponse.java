package com.AddisBuilders.Platform.DTO;

import lombok.Builder;

@Builder
public record CategoryResponse(
        Long id,
        String name,
        String description
) {}
