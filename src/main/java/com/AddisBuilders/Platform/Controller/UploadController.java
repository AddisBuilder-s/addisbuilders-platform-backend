package com.AddisBuilders.Platform.Controller;

import com.AddisBuilders.Platform.DTO.UploadResponse;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Service.UploadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class UploadController {
    private final UploadService service;

    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("purpose") String purpose,
                                                 @AuthenticationPrincipal User user,
                                                 HttpServletRequest request) {
        return ResponseEntity.status(201).body(service.upload(file, purpose, user, request));
    }

    @GetMapping("/uploads/avatars/{id}")
    public ResponseEntity<Resource> avatar(@PathVariable String id) {
        return response(service.avatar(id), true);
    }

    @GetMapping("/uploads/files/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> file(@PathVariable String id, @AuthenticationPrincipal User user) {
        return response(service.privateFile(id, user), false);
    }

    private ResponseEntity<Resource> response(UploadService.DownloadedFile file, boolean isPublic) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(file.filename(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(isPublic ? CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic() : CacheControl.noStore())
                .body(file.resource());
    }
}
