package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.Exception.ResourceNotFoundException;

import com.AddisBuilders.Platform.DTO.UploadResponse;
import com.AddisBuilders.Platform.Model.Lesson;
import com.AddisBuilders.Platform.Model.UploadedFile;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;
import com.AddisBuilders.Platform.Model.enums.EnrollmentStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Repository.EnrollmentRepository;
import com.AddisBuilders.Platform.Repository.InternshipApplicationRepository;
import com.AddisBuilders.Platform.Repository.LessonRepository;
import com.AddisBuilders.Platform.Repository.UploadedFileRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadService {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private final UploadedFileRepository files;
    private final InternshipApplicationRepository applications;
    private final LessonRepository lessons;
    private final EnrollmentRepository enrollments;

    @Value("${app.upload-dir:uploads}")
    private String uploadDirectory;

    @Transactional
    public UploadResponse upload(MultipartFile file, String purpose, User owner, HttpServletRequest request) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Choose a file to upload");
        if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("Files cannot exceed 10 MB");
        String normalizedPurpose = purpose == null ? "" : purpose.trim().toUpperCase(Locale.ROOT);
        validatePurpose(normalizedPurpose, owner);
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        String extension = extension(originalName);
        if (!allowedExtensions(normalizedPurpose).contains(extension)) {
            throw new IllegalArgumentException("This file type is not allowed for " + normalizedPurpose.toLowerCase(Locale.ROOT));
        }
        String contentType = contentType(extension);
        String id = UUID.randomUUID().toString();
        String storageName = id + "." + extension;
        Path root = Path.of(uploadDirectory).toAbsolutePath().normalize();
        Path destination = root.resolve(storageName).normalize();
        if (!destination.startsWith(root)) throw new IllegalArgumentException("Invalid file path");
        try {
            Files.createDirectories(root);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            files.save(UploadedFile.builder().id(id).owner(owner).purpose(normalizedPurpose)
                    .originalFilename(basename(originalName)).storageFilename(storageName)
                    .contentType(contentType).sizeBytes(file.getSize()).build());
        } catch (IOException | RuntimeException error) {
            try { Files.deleteIfExists(destination); } catch (IOException ignored) { }
            if (error instanceof RuntimeException runtime) throw runtime;
            throw new IllegalStateException("Unable to store the uploaded file", error);
        }
        String route = "AVATAR".equals(normalizedPurpose) ? "/uploads/avatars/" + id : "/uploads/files/" + id;
        String url = ServletUriComponentsBuilder.fromCurrentContextPath().path(route).toUriString();
        return new UploadResponse(url, basename(originalName), file.getSize());
    }

    @Transactional(readOnly = true)
    public DownloadedFile avatar(String id) {
        UploadedFile file = files.findById(id).orElseThrow(() -> new ResourceNotFoundException("File not found"));
        if (!"AVATAR".equals(file.getPurpose())) throw new ResourceNotFoundException("File not found");
        return toDownload(file);
    }

    @Transactional
    public void deleteAvatar(User owner) {
        String avatarUrl = owner.getAvatarUrl();
        if (avatarUrl == null || !avatarUrl.contains("/uploads/avatars/")) return;
        String id = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
        UploadedFile file = files.findById(id).orElse(null);
        if (file == null || !"AVATAR".equals(file.getPurpose()) || !file.getOwner().getId().equals(owner.getId())) return;
        Path path = Path.of(uploadDirectory).toAbsolutePath().normalize().resolve(file.getStorageFilename()).normalize();
        try { Files.deleteIfExists(path); }
        catch (IOException error) { throw new IllegalStateException("Unable to remove the profile photo", error); }
        files.delete(file);
    }

    @Transactional(readOnly = true)
    public DownloadedFile privateFile(String id, User viewer) {
        UploadedFile file = files.findById(id).orElseThrow(() -> new ResourceNotFoundException("File not found"));
        if (!canDownload(file, viewer)) throw new AccessDeniedException("You cannot access this file");
        return toDownload(file);
    }

    private boolean canDownload(UploadedFile file, User viewer) {
        if (viewer == null) return false;
        if (viewer.getRole() == UserRole.SUPER_ADMIN) return true;
        if (file.getOwner().getId().equals(viewer.getId())) {
            return !"COURSE_MATERIAL".equals(file.getPurpose())
                    || viewer.getRole() == UserRole.MENTOR && viewer.getMentorStatus() == ApprovalStatus.APPROVED;
        }
        return switch (file.getPurpose()) {
            case "CV" -> viewer.getRole() == UserRole.EMPLOYER
                    ? applications.existsByResumeUrlContainingAndInternship_Organization_Owner_Id(file.getId(), viewer.getId())
                    : viewer.getRole() == UserRole.INTERNSHIP_COORDINATOR
                    && viewer.getCoordinatorStatus() == ApprovalStatus.APPROVED
                    && applications.existsByResumeUrlContaining(file.getId());
            case "VERIFICATION_DOC" -> false;
            case "COURSE_MATERIAL" -> lessons.findByContentUrlContaining(file.getId()).stream().anyMatch(lesson -> canAccessLesson(lesson, viewer));
            default -> false;
        };
    }

    private boolean canAccessLesson(Lesson lesson, User viewer) {
        var course = lesson.getModule().getCourse();
        if (viewer.getRole() == UserRole.MENTOR) return viewer.getMentorStatus() == ApprovalStatus.APPROVED
                && course.getMentor().getId().equals(viewer.getId());
        return viewer.getRole() == UserRole.LEARNER && enrollments.existsByStudentIdAndCourseIdAndStatusIn(
                viewer.getId(), course.getId(), Set.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED));
    }

    private DownloadedFile toDownload(UploadedFile file) {
        Path path = Path.of(uploadDirectory).toAbsolutePath().normalize().resolve(file.getStorageFilename()).normalize();
        if (!path.startsWith(Path.of(uploadDirectory).toAbsolutePath().normalize()) || !Files.isRegularFile(path)) {
            throw new ResourceNotFoundException("File not found");
        }
        return new DownloadedFile(new FileSystemResource(path), file.getOriginalFilename(), file.getContentType());
    }

    private void validatePurpose(String purpose, User owner) {
        boolean allowed = switch (purpose) {
            case "AVATAR" -> true;
            case "CV" -> owner.getRole() == UserRole.LEARNER;
            case "VERIFICATION_DOC" -> owner.getRole() == UserRole.EMPLOYER;
            case "COURSE_MATERIAL" -> owner.getRole() == UserRole.MENTOR
                    && owner.getMentorStatus() == ApprovalStatus.APPROVED;
            default -> false;
        };
        if (!allowed) throw new AccessDeniedException("This account cannot upload files for that purpose");
    }

    private Set<String> allowedExtensions(String purpose) {
        return switch (purpose) {
            case "AVATAR" -> Set.of("png", "jpg", "jpeg", "webp");
            case "CV", "VERIFICATION_DOC" -> Set.of("pdf", "doc", "docx", "png", "jpg", "jpeg");
            case "COURSE_MATERIAL" -> Set.of("pdf", "doc", "docx", "ppt", "pptx", "zip", "mp4", "webm");
            default -> Set.of();
        };
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) throw new IllegalArgumentException("File extension is required");
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String basename(String filename) {
        String name = Path.of(filename).getFileName().toString();
        return name.length() > 255 ? name.substring(name.length() - 255) : name;
    }

    private String contentType(String extension) {
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "zip" -> "application/zip";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            default -> "application/octet-stream";
        };
    }

    public record DownloadedFile(Resource resource, String filename, String contentType) { }
}
