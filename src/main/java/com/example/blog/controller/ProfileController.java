package com.example.blog.controller;

import com.example.blog.dto.ProfileResponse;
import com.example.blog.dto.ProjectResponse;
import com.example.blog.dto.ProjectUpsertRequest;
import com.example.blog.dto.ResumeResponse;
import com.example.blog.dto.ResumeUpsertRequest;
import com.example.blog.dto.ToggleResponse;
import com.example.blog.dto.UpdateProfileRequest;
import com.example.blog.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profiles/{username}")
    public ProfileResponse getPublicProfile(@PathVariable String username) {
        return profileService.getPublicProfile(username);
    }

    @GetMapping("/profiles/me")
    public ProfileResponse getMyProfile() {
        return profileService.getMyProfile();
    }

    @PutMapping("/profiles/me")
    public ProfileResponse updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return profileService.updateMyProfile(request);
    }

    @PostMapping("/profiles/{username}/follow")
    public ToggleResponse toggleFollow(@PathVariable String username) {
        return profileService.toggleFollow(username);
    }

    @PostMapping("/profiles/me/projects")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectUpsertRequest request) {
        ProjectResponse project = profileService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @PutMapping("/profiles/me/projects/{projectId}")
    public ProjectResponse updateProject(@PathVariable Long projectId, @Valid @RequestBody ProjectUpsertRequest request) {
        return profileService.updateProject(projectId, request);
    }

    @DeleteMapping("/profiles/me/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        profileService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/profiles/me/resume")
    public ResumeResponse upsertResume(@Valid @RequestBody ResumeUpsertRequest request) {
        return profileService.upsertResume(request);
    }

    @GetMapping("/resume/{username}")
    public ResumeResponse getResume(@PathVariable String username) {
        return profileService.getResumeByUsername(username);
    }
}
