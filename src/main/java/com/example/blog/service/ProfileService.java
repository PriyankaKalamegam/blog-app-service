package com.example.blog.service;

import com.example.blog.dto.ProfileResponse;
import com.example.blog.dto.ProjectResponse;
import com.example.blog.dto.ProjectUpsertRequest;
import com.example.blog.dto.ResumeResponse;
import com.example.blog.dto.ResumeUpsertRequest;
import com.example.blog.dto.ToggleResponse;
import com.example.blog.dto.UpdateProfileRequest;
import com.example.blog.exception.NotFoundException;
import com.example.blog.model.Follow;
import com.example.blog.model.Profile;
import com.example.blog.model.Project;
import com.example.blog.model.Resume;
import com.example.blog.model.User;
import com.example.blog.repository.FollowRepository;
import com.example.blog.repository.ProfileRepository;
import com.example.blog.repository.ProjectRepository;
import com.example.blog.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final ResumeRepository resumeRepository;
    private final FollowRepository followRepository;
    private final CurrentUserService currentUserService;

    public ProfileService(
            ProfileRepository profileRepository,
            ProjectRepository projectRepository,
            ResumeRepository resumeRepository,
            FollowRepository followRepository,
            CurrentUserService currentUserService
    ) {
        this.profileRepository = profileRepository;
        this.projectRepository = projectRepository;
        this.resumeRepository = resumeRepository;
        this.followRepository = followRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getPublicProfile(String username) {
        Profile profile = profileRepository.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException("Profile not found: " + username));
        return toProfileResponse(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile() {
        User user = currentUserService.requireCurrentUser();
        Profile profile = getProfileByUserId(user.getId());
        return toProfileResponse(profile);
    }

    @Transactional
    public ProfileResponse updateMyProfile(UpdateProfileRequest request) {
        User user = currentUserService.requireCurrentUser();
        Profile profile = getProfileByUserId(user.getId());

        // Only fields present in the request are changed, which lets the UI submit partial profile edits.
        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName().trim());
        }
        if (request.getHeadline() != null) {
            profile.setHeadline(request.getHeadline().trim());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio().trim());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl().trim());
        }
        if (request.getGithubUsername() != null) {
            profile.setGithubUsername(request.getGithubUsername().trim());
        }
        if (request.getWebsiteUrl() != null) {
            profile.setWebsiteUrl(request.getWebsiteUrl().trim());
        }
        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation().trim());
        }
        if (request.getSkills() != null) {
            profile.setSkills(new HashSet<>(request.getSkills()));
        }

        Profile saved = profileRepository.save(profile);
        return toProfileResponse(saved);
    }

    @Transactional
    public ProjectResponse createProject(ProjectUpsertRequest request) {
        Profile profile = getProfileByUserId(currentUserService.requireCurrentUser().getId());

        // Projects belong to profiles, not directly to users, so portfolio data stays grouped together.
        Project project = new Project();
        project.setProfile(profile);
        applyProject(request, project);

        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectUpsertRequest request) {
        Profile profile = getProfileByUserId(currentUserService.requireCurrentUser().getId());

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        // Return 404 for another user's project to avoid leaking ownership information.
        if (!project.getProfile().getId().equals(profile.getId())) {
            throw new NotFoundException("Project not found: " + projectId);
        }

        applyProject(request, project);
        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        Profile profile = getProfileByUserId(currentUserService.requireCurrentUser().getId());

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found: " + projectId));

        if (!project.getProfile().getId().equals(profile.getId())) {
            throw new NotFoundException("Project not found: " + projectId);
        }

        projectRepository.delete(project);
    }

    @Transactional
    public ResumeResponse upsertResume(ResumeUpsertRequest request) {
        Profile profile = getProfileByUserId(currentUserService.requireCurrentUser().getId());

        // Resume is one-per-profile; update the existing row or create it on first save.
        Resume resume = resumeRepository.findByProfileId(profile.getId()).orElseGet(Resume::new);
        resume.setProfile(profile);
        resume.setFileName(request.getFileName().trim());
        resume.setFileUrl(request.getFileUrl().trim());
        resume.setSummary(request.getSummary() != null ? request.getSummary().trim() : null);

        return toResumeResponse(resumeRepository.save(resume));
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResumeByUsername(String username) {
        Profile profile = profileRepository.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException("Profile not found: " + username));

        Resume resume = resumeRepository.findByProfileId(profile.getId())
                .orElseThrow(() -> new NotFoundException("Resume not found for " + username));

        return toResumeResponse(resume);
    }

    @Transactional
    public ToggleResponse toggleFollow(String username) {
        User currentUser = currentUserService.requireCurrentUser();
        Profile targetProfile = profileRepository.findByUserUsername(username)
                .orElseThrow(() -> new NotFoundException("Profile not found: " + username));

        User targetUser = targetProfile.getUser();
        if (targetUser.getId().equals(currentUser.getId())) {
            return new ToggleResponse(targetUser.getId(), false, "Cannot follow yourself");
        }

        // Follow behaves like a toggle so the frontend can use one button for follow/unfollow.
        boolean exists = followRepository.existsByFollowerIdAndFollowingId(currentUser.getId(), targetUser.getId());
        if (exists) {
            Follow existingFollow = followRepository.findByFollowerIdAndFollowingId(currentUser.getId(), targetUser.getId())
                    .orElseThrow(() -> new NotFoundException("Follow relation not found"));
            followRepository.delete(existingFollow);
            return new ToggleResponse(targetUser.getId(), false, "Unfollowed");
        }

        Follow follow = new Follow();
        follow.setFollower(currentUser);
        follow.setFollowing(targetUser);
        followRepository.save(follow);

        return new ToggleResponse(targetUser.getId(), true, "Following");
    }

    private void applyProject(ProjectUpsertRequest request, Project project) {
        // Keep project mapping in one helper so create and update stay behaviorally identical.
        project.setName(request.getName().trim());
        project.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        project.setRepositoryUrl(request.getRepositoryUrl() != null ? request.getRepositoryUrl().trim() : null);
        project.setLiveUrl(request.getLiveUrl() != null ? request.getLiveUrl().trim() : null);
        project.setTechStack(request.getTechStack() != null ? request.getTechStack().trim() : null);
        project.setFeatured(request.isFeatured());
    }

    private Profile getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Profile not found for user: " + userId));
    }

    private ProfileResponse toProfileResponse(Profile profile) {
        // Aggregate counts and child collections into the public profile view consumed by React.
        long followerCount = followRepository.countByFollowingId(profile.getUser().getId());
        long followingCount = followRepository.countByFollowerId(profile.getUser().getId());

        List<ProjectResponse> projects = projectRepository.findByProfileIdOrderByFeaturedDescCreatedAtDesc(profile.getId())
                .stream()
                .map(this::toProjectResponse)
                .toList();

        ResumeResponse resume = resumeRepository.findByProfileId(profile.getId())
                .map(this::toResumeResponse)
                .orElse(null);

        return new ProfileResponse(
                profile.getUser().getUsername(),
                profile.getUser().getEmail(),
                profile.getDisplayName(),
                profile.getHeadline(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getGithubUsername(),
                profile.getWebsiteUrl(),
                profile.getLocation(),
                profile.getSkills(),
                followerCount,
                followingCount,
                projects,
                resume
        );
    }

    private ProjectResponse toProjectResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getRepositoryUrl(),
                project.getLiveUrl(),
                project.getTechStack(),
                project.isFeatured(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    private ResumeResponse toResumeResponse(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getFileName(),
                resume.getFileUrl(),
                resume.getSummary(),
                resume.getUpdatedAt()
        );
    }
}
