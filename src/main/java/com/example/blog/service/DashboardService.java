package com.example.blog.service;

import com.example.blog.dto.ActivityItemResponse;
import com.example.blog.dto.DashboardResponse;
import com.example.blog.model.Post;
import com.example.blog.model.enums.PostStatus;
import com.example.blog.repository.PostLikeRepository;
import com.example.blog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CurrentUserService currentUserService;

    public DashboardService(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            CurrentUserService currentUserService
    ) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getMyDashboard() {
        Long userId = currentUserService.requireCurrentUser().getId();
        List<Post> myPosts = postRepository.findByAuthorIdOrderByUpdatedAtDesc(userId);

        // Dashboard metrics are derived from the author's posts so they always reflect current content.
        long totalPosts = myPosts.size();
        long totalViews = myPosts.stream().mapToLong(post -> post.getViewCount() == null ? 0 : post.getViewCount()).sum();
        long totalLikes = myPosts.stream().mapToLong(post -> postLikeRepository.countByPostId(post.getId())).sum();
        long drafts = myPosts.stream().filter(post -> post.getStatus() == PostStatus.DRAFT).count();

        // Recent activity is intentionally lightweight; the UI only needs a label and timestamp.
        List<ActivityItemResponse> activity = myPosts.stream()
                .limit(10)
                .map(post -> new ActivityItemResponse(
                        "post",
                        post.getTitle(),
                        post.getUpdatedAt()
                ))
                .sorted(Comparator.comparing(ActivityItemResponse::createdAt).reversed())
                .limit(12)
                .toList();

        return new DashboardResponse(totalPosts, totalViews, totalLikes, drafts, activity);
    }
}
