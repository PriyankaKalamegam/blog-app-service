package com.example.blog.dto;

public record TagResponse(
        Long id,
        String name,
        long usageCount
) {
}
