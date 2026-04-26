package com.example.blog.controller;

import com.example.blog.dto.CommentResponse;
import com.example.blog.dto.CreateCommentRequest;
import com.example.blog.dto.CreatePostRequest;
import com.example.blog.dto.LikeResponse;
import com.example.blog.dto.PostDetailResponse;
import com.example.blog.dto.PostSummaryResponse;
import com.example.blog.dto.ToggleResponse;
import com.example.blog.dto.UpdatePostRequest;
import com.example.blog.service.PostService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<PostSummaryResponse> getAllPosts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tag
    ) {
        return postService.getAllPosts(search, tag);
    }

    @GetMapping("/{postId}")
    public PostDetailResponse getPostById(@PathVariable Long postId) {
        return postService.getPostById(postId);
    }

    @PostMapping
    public ResponseEntity<PostDetailResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        PostDetailResponse created = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{postId}")
    public PostDetailResponse updatePost(@PathVariable Long postId, @Valid @RequestBody UpdatePostRequest request) {
        return postService.updatePost(postId, request);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentResponse comment = postService.addComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PostMapping("/{postId}/like")
    public LikeResponse incrementLike(@PathVariable Long postId) {
        return postService.incrementLike(postId);
    }

    @PostMapping("/{postId}/bookmark")
    public ToggleResponse toggleBookmark(@PathVariable Long postId) {
        return postService.toggleBookmark(postId);
    }

    @GetMapping("/bookmarks")
    public List<PostSummaryResponse> getBookmarks() {
        return postService.getBookmarkedPosts();
    }
}
