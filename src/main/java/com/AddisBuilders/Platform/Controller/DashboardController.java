package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.LearnerDashboardResponse;
import com.AddisBuilders.Platform.DTO.MentorDashboardResponse;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/learner")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<LearnerDashboardResponse> learner(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.learnerSummary(user));
    }

    @GetMapping("/mentor")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @accountAuthorization.isApprovedMentor(authentication)")
    public ResponseEntity<MentorDashboardResponse> mentor(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dashboardService.mentorSummary(user));
    }
}
