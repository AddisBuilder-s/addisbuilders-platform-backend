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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        // 2. Build and save the User entity
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .coordinatorStatus(request.role() == UserRole.INTERNSHIP_COORDINATOR ? ApprovalStatus.PENDING : null)
                .build();

        User savedUser = userRepository.save(user);

        // 3. If registered as an EMPLOYER, create their organization record
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

        // 4. Generate JWT token for immediate login after registration
        String jwtToken = jwtUtils.generateToken(savedUser);

        return AuthResponse.builder()
                .token(jwtToken)
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .build();
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

        return AuthResponse.builder()
                .token(jwtToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}