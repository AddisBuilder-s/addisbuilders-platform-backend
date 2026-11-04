package com.AddisBuilders.Platform.DTO;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationResponse(Long id, Long userId, String title, String message,
                                   String type, String link, boolean isRead, LocalDateTime createdAt) {}
