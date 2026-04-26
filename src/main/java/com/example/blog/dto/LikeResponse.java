package com.example.blog.dto;

public record LikeResponse(
        Long postId,
        Integer likeCount,
        boolean likedByCurrentUser
) {
}
