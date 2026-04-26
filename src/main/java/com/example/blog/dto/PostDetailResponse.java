package com.example.blog.dto;

import com.example.blog.model.enums.PostStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record PostDetailResponse(
        Long id,
        String title,
        String slug,
        String excerpt,
        String content,
        String author,
        Integer likeCount,
        Integer viewCount,
        PostStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime publishedAt,
        Set<String> tags,
        List<CommentResponse> comments,
        Boolean bookmarkedByCurrentUser,
        Boolean likedByCurrentUser
) {
}
