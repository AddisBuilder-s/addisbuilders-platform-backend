package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.NotificationResponse;
import com.AddisBuilders.Platform.DTO.NotificationUnreadCountResponse;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(service.list(user, unreadOnly));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationUnreadCountResponse> unreadCount(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(new NotificationUnreadCountResponse(service.unreadCount(user)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        service.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> clear(@AuthenticationPrincipal User user) {
        service.clear(user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
        service.markRead(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal User user) {
        service.markAllRead(user);
        return ResponseEntity.noContent().build();
    }
}
