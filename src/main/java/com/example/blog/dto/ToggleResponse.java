package com.example.blog.dto;

public record ToggleResponse(
        Long targetId,
        boolean enabled,
        String message
) {
}
