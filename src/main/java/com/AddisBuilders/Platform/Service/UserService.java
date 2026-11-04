package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.Exception.ResourceNotFoundException;

import com.AddisBuilders.Platform.DTO.UpdateUserProfileRequest;
import com.AddisBuilders.Platform.DTO.UserResponse;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Model.enums.UserStatus;
import com.AddisBuilders.Platform.Repository.OrganizationRepository;
import com.AddisBuilders.Platform.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository users;
    private final OrganizationRepository organizations;

    @Transactional(readOnly = true)
    public UserResponse getProfile(User user) { return toResponse(user); }

    @Transactional
    public UserResponse updateProfile(User user, UpdateUserProfileRequest request) {
        if (request.fullName() != null) {
            String name = request.fullName().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("Name cannot be blank");
            user.setFullName(name);
        }
        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            if (users.existsByEmail(request.email())) throw new com.AddisBuilders.Platform.Exception.ConflictException("Email is already in use");
            user.setEmail(request.email().trim().toLowerCase());
        }
        if (request.phone() != null) user.setPhone(request.phone().trim());
        if (request.bio() != null) user.setBio(request.bio());
        if (request.university() != null) user.setUniversity(request.university().trim());
        if (request.department() != null) user.setDepartment(request.department().trim());
        if (request.studentId() != null) user.setStudentId(request.studentId().trim());
        if (request.cvUrl() != null) user.setCvUrl(request.cvUrl().trim());
        return toResponse(users.save(user));
    }

    @Transactional
    public UserResponse setAvatar(User user, String url) {
        user.setAvatarUrl(url);
        return toResponse(users.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAll() {
        return users.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse toggleStatus(Long userId, User actor) {
        User target = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (target.getRole() == UserRole.SUPER_ADMIN && target.getId().equals(actor.getId())) {
            throw new AccessDeniedException("You cannot suspend your own administrator account");
        }
        target.setStatus(target.getStatus() == UserStatus.ACTIVE ? UserStatus.SUSPENDED : UserStatus.ACTIVE);
        return toResponse(users.save(target));
    }

    public UserResponse toResponse(User user) {
        Long organizationId = organizations.findByOwnerId(user.getId()).map(org -> org.getId()).orElse(null);
        return UserResponse.builder().id(user.getId()).email(user.getEmail()).fullName(user.getFullName())
                .role(user.getRole()).status(user.getStatus()).phone(user.getPhone()).bio(user.getBio())
                .university(user.getUniversity()).department(user.getDepartment()).studentId(user.getStudentId())
                .cvUrl(user.getCvUrl()).avatarUrl(user.getAvatarUrl()).coordinatorStatus(user.getCoordinatorStatus())
                .mentorStatus(user.getMentorStatus())
                .employeeId(user.getEmployeeId()).proofDocumentUrl(user.getProofDocumentUrl())
                .organizationId(organizationId).isActive(user.getStatus() == UserStatus.ACTIVE)
                .createdAt(user.getCreatedAt()).build();
    }
}
