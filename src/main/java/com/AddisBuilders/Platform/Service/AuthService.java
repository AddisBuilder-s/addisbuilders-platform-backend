package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.AuthResponse;
import com.AddisBuilders.Platform.DTO.LoginRequest;
import com.AddisBuilders.Platform.DTO.RegisterRequest;
import com.AddisBuilders.Platform.Model.*;
import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Model.enums.UserStatus;
import com.AddisBuilders.Platform.Model.enums.VerificationStatus;
import com.AddisBuilders.Platform.Repository.OrganizationRepository;
import com.AddisBuilders.Platform.Repository.UserRepository;
import com.AddisBuilders.Platform.Security.JwtUtils;
import lombok.RequiredArgsConstructor;import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.net.URI;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new com.AddisBuilders.Platform.Exception.ConflictException("Error: Email is already in use!");
        }

        // 2. Prevent self-registration as a privileged administrator
        if (request.role() == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException("Registration as SUPER_ADMIN is not permitted.");
        }
        if (request.role() == UserRole.MENTOR) {
            requireFacultyCredentials(request.university(), request.department(), request.employeeId(), request.proofDocumentUrl());
        }

        // 3. Build and save the User entity
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .coordinatorStatus(request.role() == UserRole.INTERNSHIP_COORDINATOR ? ApprovalStatus.PENDING : null)
                .mentorStatus(request.role() == UserRole.MENTOR ? ApprovalStatus.PENDING : null)
                .phone(request.phone())
                .university(request.university())
                .department(request.department())
                .studentId(request.studentId())
                .employeeId(request.employeeId())
                .proofDocumentUrl(request.proofDocumentUrl())
                .build();

        User savedUser = userRepository.save(user);

        // 4. If registered as an EMPLOYER, create their organization record
        if (request.role() == UserRole.EMPLOYER) {
            Organization organization = Organization.builder()
                    .owner(savedUser)
                    .name(request.organizationName() != null ? request.organizationName() : savedUser.getFullName() + "'s Organization")
                    .description(request.organizationDescription())
                    .website(request.organizationWebsite())
                    .verificationStatus(VerificationStatus.PENDING)
                    .build();
            organizationRepository.save(organization);
        }

        // 5. Generate JWT token for immediate login after registration
        String jwtToken = jwtUtils.generateToken(savedUser);

        return toAuthResponse(savedUser, jwtToken);
    }

    public AuthResponse login(LoginRequest request) {
        // 1. Authenticate credentials via Spring Security AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. Extract authenticated user details
        User user = (User) authentication.getPrincipal();

        // 3. Generate JWT token
        String jwtToken = jwtUtils.generateToken(user);

        return toAuthResponse(user, jwtToken);
    }

    /**
     * Returns the current profile in the same shape as login/register, without
     * minting a new token.
     */
    @Transactional(readOnly = true)
    public AuthResponse currentUser(User user) {
        return toAuthResponse(user, null);
    }

    private AuthResponse toAuthResponse(User user, String token) {
        Long organizationId = organizationRepository.findByOwnerId(user.getId())
                .map(Organization::getId)
                .orElse(null);

        return AuthResponse.builder()
                .id(user.getId())
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .coordinatorStatus(user.getCoordinatorStatus())
                .mentorStatus(user.getMentorStatus())
                .university(user.getUniversity())
                .department(user.getDepartment())
                .employeeId(user.getEmployeeId())
                .proofDocumentUrl(user.getProofDocumentUrl())
                .phone(user.getPhone())
                .bio(user.getBio())
                .studentId(user.getStudentId())
                .cvUrl(user.getCvUrl())
                .avatarUrl(user.getAvatarUrl())
                .organizationId(organizationId)
                .createdAt(user.getCreatedAt())
                .isActive(user.getStatus() == UserStatus.ACTIVE)
                .build();
    }

    private void requireFacultyCredentials(String university, String department, String employeeId, String proofUrl) {
        if (!StringUtils.hasText(university) || !StringUtils.hasText(department)
                || !StringUtils.hasText(employeeId) || !StringUtils.hasText(proofUrl)) {
            throw new IllegalArgumentException("Mentor applications require an institution, department, staff ID, and proof document URL.");
        }
        try {
            URI uri = URI.create(proofUrl.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || !StringUtils.hasText(uri.getHost())) {
                throw new IllegalArgumentException("Mentor proof document must be a secure HTTPS URL.");
            }
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Mentor proof document must be a valid secure HTTPS URL.");
        }
    }
}
