package com.example.blog.dto;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        String repositoryUrl,
        String liveUrl,
        String techStack,
        boolean featured,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
