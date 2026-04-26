package com.example.blog.dto;

public record AuthResponse(
        String token,
        long expiresInSeconds,
        CurrentUserResponse user
) {
}
