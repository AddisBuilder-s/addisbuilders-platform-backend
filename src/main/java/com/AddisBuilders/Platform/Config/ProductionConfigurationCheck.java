package com.AddisBuilders.Platform.Config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProductionConfigurationCheck {
    @Value("${JWT_SECRET:}")
    private String jwtSecret;

    @Value("${DB_PASSWORD:}")
    private String databasePassword;

    @Value("${DB_USERNAME:}")
    private String databaseUsername;

    @Value("${DB_URL:}")
    private String databaseUrl;

    @Value("${CORS_ALLOWED_ORIGINS:}")
    private String allowedOrigins;

    @PostConstruct
    void validate() {
        if (jwtSecret == null || jwtSecret.trim().length() < 32) {
            throw new IllegalStateException("Production requires JWT_SECRET with at least 32 characters");
        }
        if (databasePassword == null || databasePassword.isBlank() || "root".equalsIgnoreCase(databasePassword)
                || databaseUsername == null || databaseUsername.isBlank() || "root".equalsIgnoreCase(databaseUsername)) {
            throw new IllegalStateException("Production requires a dedicated DB_PASSWORD");
        }
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalStateException("Production requires DB_URL to point at its managed database");
        }
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            throw new IllegalStateException("Production requires CORS_ALLOWED_ORIGINS");
        }
    }
}
