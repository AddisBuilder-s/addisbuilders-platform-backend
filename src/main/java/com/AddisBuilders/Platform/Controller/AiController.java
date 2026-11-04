package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.AiAnswerResponse;
import com.AddisBuilders.Platform.DTO.AiQuestionRequest;
import com.AddisBuilders.Platform.DTO.InternshipMatchResponse;
import com.AddisBuilders.Platform.DTO.ProgressSummaryResponse;
import com.AddisBuilders.Platform.DTO.StudyPlanResponse;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.AiAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Optional Project-4 AI support endpoints. All answers are generated with
 * deterministic heuristics over platform data; no external provider is called.
 */
@RestController
@RequestMapping("/ai")
@PreAuthorize("hasRole('LEARNER')")
@RequiredArgsConstructor
public class AiController {

    private final AiAssistantService service;

    @GetMapping("/study-plan")
    public ResponseEntity<StudyPlanResponse> studyPlan(
            @RequestParam Long courseId,
            @AuthenticationPrincipal User learner) {
        return ResponseEntity.ok(service.studyPlan(courseId, learner));
    }

    @GetMapping("/progress-summary")
    public ResponseEntity<ProgressSummaryResponse> progressSummary(@AuthenticationPrincipal User learner) {
        return ResponseEntity.ok(service.progressSummary(learner));
    }

    @GetMapping("/internship-matches")
    public ResponseEntity<List<InternshipMatchResponse>> internshipMatches(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal User learner) {
        return ResponseEntity.ok(service.internshipMatches(learner, limit));
    }

    @PostMapping("/ask")
    public ResponseEntity<AiAnswerResponse> ask(
            @Valid @RequestBody AiQuestionRequest request,
            @AuthenticationPrincipal User learner) {
        return ResponseEntity.ok(service.ask(request.question(), request.courseId(), learner));
    }
}
