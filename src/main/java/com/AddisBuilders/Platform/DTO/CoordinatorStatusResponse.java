package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;

public record CoordinatorStatusResponse(ApprovalStatus status, String notes) {}
