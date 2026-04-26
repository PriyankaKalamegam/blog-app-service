package com.example.blog.dto;

public record AdminOverviewResponse(
        long users,
        long posts,
        long comments,
        long tags
) {
}
