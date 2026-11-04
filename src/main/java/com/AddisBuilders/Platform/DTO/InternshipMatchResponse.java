package com.AddisBuilders.Platform.DTO;

import lombok.Builder;

import java.util.List;

@Builder
public record InternshipMatchResponse(
        Long internshipId,
        String title,
        String organizationName,
        String category,
        String workMode,
        String location,
        double matchScore,
        List<String> reasons
) {}
