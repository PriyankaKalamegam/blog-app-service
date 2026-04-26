package com.example.blog.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String author,
        String text,
        LocalDateTime createdAt
) {
}
