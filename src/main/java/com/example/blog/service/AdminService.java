package com.example.blog.service;

import com.example.blog.dto.AdminOverviewResponse;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.TagRepository;
import com.example.blog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;

    public AdminService(
            UserRepository userRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            TagRepository tagRepository
    ) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public AdminOverviewResponse getOverview() {
        // Admin overview is a compact health snapshot for moderation and platform monitoring.
        return new AdminOverviewResponse(
                userRepository.count(),
                postRepository.count(),
                commentRepository.count(),
                tagRepository.count()
        );
    }
}
