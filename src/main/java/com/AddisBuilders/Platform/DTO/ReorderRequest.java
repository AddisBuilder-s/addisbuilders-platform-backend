package com.AddisBuilders.Platform.DTO;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Ordered list of resource ids. The index in the list becomes the new order
 * (starting at 1) when the curriculum is re-sequenced.
 */
public record ReorderRequest(
        @NotEmpty(message = "The ordered id list cannot be empty")
        List<Long> ids
) {}
