package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for changing the status of an enrollment.
 */
public record UpdateEnrollmentStatusRequest(
        @NotNull(message = "Enrollment status is required")
        EnrollmentStatus status
) {}
