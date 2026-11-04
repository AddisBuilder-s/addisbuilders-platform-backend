package com.AddisBuilders.Platform.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @Size(max = 150) String fullName,
        @Email @Size(max = 255) String email,
        @Size(max = 40) String phone,
        String bio,
        @Size(max = 200) String university,
        @Size(max = 200) String department,
        @Size(max = 100) String studentId,
        @Size(max = 1000) String cvUrl
) {}
