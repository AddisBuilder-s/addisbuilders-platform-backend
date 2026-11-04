package com.AddisBuilders.Platform.DTO;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AuthResponse(
        Long id,
        String token,
        String type,
        String email,
        String fullName,
        UserRole role,
        ApprovalStatus coordinatorStatus,
        ApprovalStatus mentorStatus,
        String university,
        String department,
        String employeeId,
        String proofDocumentUrl,
        String phone,
        String bio,
        String studentId,
        String cvUrl,
        String avatarUrl,
        Long organizationId,
        LocalDateTime createdAt,
        boolean isActive
) {
    public AuthResponse {
        type = type != null ? type : "Bearer";
    }
}
