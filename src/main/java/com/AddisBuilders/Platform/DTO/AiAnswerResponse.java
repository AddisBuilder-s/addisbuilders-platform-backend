package com.AddisBuilders.Platform.DTO;

import lombok.Builder;

import java.util.List;

@Builder
public record AiAnswerResponse(
        String answer,
        List<String> sources,
        boolean aiGenerated
) {}
