package com.example.blog.dto;

import java.time.LocalDateTime;

public record ActivityItemResponse(
        String type,
        String title,
        LocalDateTime createdAt
) {
}
