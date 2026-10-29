package com.AddisBuilders.Platform.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplyInternshipRequest(
        String coverLetter,

        @NotBlank(message = "Resume URI/Link is required")
        @Size(max = 500, message = "Resume URI cannot exceed 500 characters")
        String resumeUri
) {}
