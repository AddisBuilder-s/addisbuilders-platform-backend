package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Model.enums.UserStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponse(
        Long id, String email, String fullName, UserRole role, UserStatus status,
        String phone, String bio, String university, String department, String studentId,
        String cvUrl, String avatarUrl, ApprovalStatus coordinatorStatus, ApprovalStatus mentorStatus,
        String employeeId, String proofDocumentUrl, Long organizationId,
        boolean isActive, LocalDateTime createdAt
) {}
