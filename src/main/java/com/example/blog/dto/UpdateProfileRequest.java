package com.example.blog.dto;

import jakarta.validation.constraints.Size;

import java.util.Set;

public class UpdateProfileRequest {

    @Size(max = 120)
    private String displayName;

    @Size(max = 220)
    private String headline;

    @Size(max = 2500)
    private String bio;

    @Size(max = 500)
    private String avatarUrl;

    @Size(max = 120)
    private String githubUsername;

    @Size(max = 500)
    private String websiteUrl;

    @Size(max = 120)
    private String location;

    private Set<@Size(max = 80) String> skills;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Set<String> getSkills() {
        return skills;
    }

    public void setSkills(Set<String> skills) {
        this.skills = skills;
    }
}
