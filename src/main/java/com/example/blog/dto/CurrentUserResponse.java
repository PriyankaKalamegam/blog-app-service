package com.example.blog.dto;

import com.example.blog.model.enums.Role;

public record CurrentUserResponse(
        Long id,
        String username,
        String email,
        Role role,
        String displayName,
        String avatarUrl
) {
}
