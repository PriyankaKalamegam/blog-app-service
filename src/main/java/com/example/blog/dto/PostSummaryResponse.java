package com.example.blog.dto;

import com.example.blog.model.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record PostSummaryResponse(
        Long id,
        String title,
        String slug,
        String excerpt,
        String author,
        Integer likeCount,
        Integer viewCount,
        Integer commentCount,
        PostStatus status,
        LocalDateTime createdAt,
        LocalDateTime publishedAt,
        Set<String> tags,
        Boolean bookmarkedByCurrentUser,
        Boolean likedByCurrentUser
) {
}
