package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;

public record MentorStatusResponse(ApprovalStatus status, String notes) {}
