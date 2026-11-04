package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.AuditLogResponse;
import com.AddisBuilders.Platform.Model.AuditLog;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository repository;

    @Transactional
    public void record(User actor, String action, String targetType, Object targetId, String details,
                       HttpServletRequest request) {
        repository.save(AuditLog.builder()
                .actorId(actor == null ? null : actor.getId())
                .actorName(actor == null ? "System" : actor.getFullName())
                .actorRole(actor == null ? "SYSTEM" : actor.getRole().name())
                .action(action)
                .targetType(targetType)
                .targetId(targetId == null ? null : String.valueOf(targetId))
                .details(details)
                .ipAddress(request == null ? null : request.getRemoteAddr())
                .build());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> listRecent() {
        return repository.findTop500ByOrderByCreatedAtDesc().stream().map(log -> AuditLogResponse.builder()
                .id(log.getId())
                .actorId(log.getActorId() == null ? null : String.valueOf(log.getActorId()))
                .actorName(log.getActorName())
                .actorRole(log.getActorRole())
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .details(log.getDetails())
                .timestamp(log.getCreatedAt())
                .ipAddress(log.getIpAddress())
                .build()).toList();
    }
}
