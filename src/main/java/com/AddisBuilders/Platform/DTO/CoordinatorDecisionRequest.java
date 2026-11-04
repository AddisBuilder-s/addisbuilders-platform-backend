package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;

public record CoordinatorDecisionRequest(@NotNull ApprovalStatus status, String notes) {}
