package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;

public record VerificationDecisionRequest(@NotNull VerificationStatus status, String notes) {}
