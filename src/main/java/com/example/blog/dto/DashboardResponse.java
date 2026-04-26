package com.example.blog.dto;

import java.util.List;

public record DashboardResponse(
        long totalPosts,
        long totalViews,
        long totalLikes,
        long drafts,
        List<ActivityItemResponse> recentActivity
) {
}
