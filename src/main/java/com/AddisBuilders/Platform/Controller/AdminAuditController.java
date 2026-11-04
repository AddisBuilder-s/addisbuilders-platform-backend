package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.AuditLogResponse;
import com.AddisBuilders.Platform.Service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminAuditController {
    private final AuditLogService service;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> list() {
        return ResponseEntity.ok(service.listRecent());
    }
}
