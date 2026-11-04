package com.AddisBuilders.Platform.DTO;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request payload for renaming and/or re-ordering an existing module.
 * Every field is optional so the endpoint can be used for a pure rename or a
 * pure re-order.
 */
public record UpdateModuleRequest(
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        String title,

        @Positive(message = "Module order must be a positive integer")
        Integer moduleOrder
) {}
