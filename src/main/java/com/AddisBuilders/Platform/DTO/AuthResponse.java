package com.AddisBuilders.Platform.DTO;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import lombok.Builder;

@Builder
public record AuthResponse(
        String token,
        String type,
        String email,
        String fullName,
        UserRole role
) {
    // Compact constructor to set a default value for 'type' if not specified
    public AuthResponse(String token, String type, String email, String fullName, UserRole role) {
        this.token = token;
        this.type = (type != null) ? type : "Bearer";
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }
}