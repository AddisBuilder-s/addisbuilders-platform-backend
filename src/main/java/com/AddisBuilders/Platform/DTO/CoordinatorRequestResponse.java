package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CoordinatorRequestResponse(
        Long id, String userId, String fullName, String email, String university,
        String department, String employeeId, String proofDocumentUrl,
        ApprovalStatus status, LocalDateTime requestDate, LocalDateTime decisionDate,
        String decisionNotes
) {}
