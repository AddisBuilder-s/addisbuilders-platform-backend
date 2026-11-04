package com.AddisBuilders.Platform.DTO;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AuditLogResponse(Long id, String actorId, String actorName, String actorRole,
                               String action, String targetType, String targetId,
                               String details, LocalDateTime timestamp, String ipAddress) {}
