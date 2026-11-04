package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.UpdateUserProfileRequest;
import com.AddisBuilders.Platform.DTO.UserResponse;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.UserService;
import com.AddisBuilders.Platform.Service.UploadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final UploadService uploads;

    @GetMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.getProfile(user));
    }

    @PatchMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UpdateUserProfileRequest request,
                                                   @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.updateProfile(user, request));
    }

    @PostMapping(value = "/users/me/profile-photo", consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> uploadProfilePhoto(@RequestParam("photo") MultipartFile photo,
                                                            @AuthenticationPrincipal User user,
                                                            HttpServletRequest request) {
        String url = uploads.upload(photo, "AVATAR", user, request).url();
        return ResponseEntity.ok(service.setAvatar(user, url));
    }

    @DeleteMapping("/users/me/profile-photo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> removeProfilePhoto(@AuthenticationPrincipal User user) {
        uploads.deleteAvatar(user);
        return ResponseEntity.ok(service.setAvatar(user, null));
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(service.listAll());
    }

    @PatchMapping("/admin/users/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserResponse> toggleStatus(@PathVariable Long id, @AuthenticationPrincipal User actor) {
        return ResponseEntity.ok(service.toggleStatus(id, actor));
    }
}
