package com.AddisBuilders.Platform.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata for the generated contract. With the application context
 * path set to {@code /api}, the live documents are available at
 * {@code /api/v3/api-docs} and {@code /api/swagger-ui.html}.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AddisBuilders University Learning & Internship Platform API",
                version = "1.0.0",
                description = "REST API for learners, mentors, internship coordinators, employers and administrators. "
                        + "All paths below are relative to the /api context path.",
                contact = @Contact(name = "AddisBuilders Platform Team")
        ),
        servers = {
                @Server(url = "/api", description = "Current host (context path included)")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Paste the raw JWT returned by POST /auth/login or POST /auth/register."
)
public class OpenApiConfig {
}
