package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.AchievementResponse;
import com.AddisBuilders.Platform.DTO.LeaderboardEntryResponse;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService service;

    @GetMapping("/leaderboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LeaderboardEntryResponse>> leaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.leaderboard(limit));
    }

    @GetMapping("/leaderboard/me")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<LeaderboardEntryResponse> myRank(@AuthenticationPrincipal User learner) {
        return ResponseEntity.ok(service.myEntry(learner));
    }

    @GetMapping("/achievements/me")
    @PreAuthorize("hasRole('LEARNER')")
    public ResponseEntity<List<AchievementResponse>> myAchievements(@AuthenticationPrincipal User learner) {
        return ResponseEntity.ok(service.achievements(learner));
    }
}
