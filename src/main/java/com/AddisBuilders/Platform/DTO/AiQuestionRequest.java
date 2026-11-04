package com.AddisBuilders.Platform.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AiQuestionRequest(
        @NotBlank(message = "Question is required")
        @Size(max = 1000, message = "Question cannot exceed 1000 characters")
        String question,
        Long courseId
) {}

