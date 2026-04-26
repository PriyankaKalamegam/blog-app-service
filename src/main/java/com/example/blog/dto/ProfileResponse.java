package com.example.blog.dto;

import java.util.List;
import java.util.Set;

public record ProfileResponse(
        String username,
        String email,
        String displayName,
        String headline,
        String bio,
        String avatarUrl,
        String githubUsername,
        String websiteUrl,
        String location,
        Set<String> skills,
        long followerCount,
        long followingCount,
        List<ProjectResponse> projects,
        ResumeResponse resume
) {
}
