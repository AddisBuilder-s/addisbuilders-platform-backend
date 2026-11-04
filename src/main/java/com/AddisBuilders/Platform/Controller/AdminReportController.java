package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.AdminReportResponse;
import com.AddisBuilders.Platform.Service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminReportController {
    private final AdminReportService service;

    @GetMapping("/overview")
    public ResponseEntity<AdminReportResponse> overview() {
        return ResponseEntity.ok(service.overview());
    }
}
