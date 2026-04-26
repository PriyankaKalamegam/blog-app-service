package com.example.blog.dto;

import java.time.LocalDateTime;

public record ResumeResponse(
        Long id,
        String fileName,
        String fileUrl,
        String summary,
        LocalDateTime updatedAt
) {
}
