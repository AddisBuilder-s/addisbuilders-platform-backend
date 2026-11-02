package com.AddisBuilders.Platform.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplyInternshipRequest(
        @NotBlank(message = "A cover letter is required")
        @Size(min = 30, max = 5000, message = "The cover letter must be between 30 and 5000 characters")
        String coverLetter,

        @NotBlank(message = "Resume URI/Link is required")
        @Size(max = 500, message = "Resume URI cannot exceed 500 characters")
        String resumeUri
) {}
