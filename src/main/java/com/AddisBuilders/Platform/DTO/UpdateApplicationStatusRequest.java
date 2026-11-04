package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateApplicationStatusRequest(@NotNull ApplicationStatus status, String notes) {}
