package com.AddisBuilders.Platform.DTO;

import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 150)
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        @NotNull(message = "User role is required")
        UserRole role,
        // Organization fields (for EMPLOYER / COORDINATOR)
        String organizationName,
        String organizationDescription,
        String organizationWebsite


) {}