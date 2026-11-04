package com.AddisBuilders.Platform.DTO;

import lombok.Builder;

@Builder
public record AchievementResponse(
        String code,
        String title,
        String description,
        boolean earned,
        String progressHint
) {}
