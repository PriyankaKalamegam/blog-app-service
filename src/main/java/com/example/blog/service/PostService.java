package com.example.blog.service;

import com.example.blog.dto.CommentResponse;
import com.example.blog.dto.CreateCommentRequest;
import com.example.blog.dto.CreatePostRequest;
import com.example.blog.dto.LikeResponse;
import com.example.blog.dto.PostDetailResponse;
import com.example.blog.dto.PostSummaryResponse;
import com.example.blog.dto.ToggleResponse;
import com.example.blog.dto.UpdatePostRequest;
import com.example.blog.exception.BadRequestException;
import com.example.blog.exception.NotFoundException;
import com.example.blog.model.Bookmark;
import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.model.PostLike;
import com.example.blog.model.Tag;
import com.example.blog.model.User;
import com.example.blog.model.enums.PostStatus;
import com.example.blog.model.enums.Role;
import com.example.blog.repository.BookmarkRepository;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostLikeRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CurrentUserService currentUserService;

    public PostService(
            PostRepository postRepository,
            TagRepository tagRepository,
            CommentRepository commentRepository,
            PostLikeRepository postLikeRepository,
            BookmarkRepository bookmarkRepository,
            CurrentUserService currentUserService
    ) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.commentRepository = commentRepository;
        this.postLikeRepository = postLikeRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getAllPosts(String search, String tag) {
        List<Post> posts = (search == null || search.isBlank())
                ? postRepository.findAllByStatusOrderByCreatedAtDesc(PostStatus.PUBLISHED)
                : postRepository.searchPublishedByTerm(PostStatus.PUBLISHED, search.trim());

        if (tag != null && !tag.isBlank()) {
            String normalizedTag = normalizeTag(tag);
            posts = posts.stream()
                    .filter(post -> post.getTags().stream().anyMatch(t -> t.getName().equals(normalizedTag)))
                    .toList();
        }

        User currentUser = currentUserService.getCurrentUserOptional().orElse(null);
        return posts.stream()
                .map(post -> toPostSummaryResponse(post, currentUser))
                .toList();
    }

    @Transactional
    public PostDetailResponse getPostById(Long postId) {
        User currentUser = currentUserService.getCurrentUserOptional().orElse(null);
        Post post = findReadablePost(postId, currentUser);
        post.setViewCount(post.getViewCount() + 1);

        return toPostDetailResponse(postRepository.save(post), currentUser);
    }

    @Transactional
    public PostDetailResponse createPost(CreatePostRequest request) {
        User currentUser = currentUserService.requireCurrentUser();

        Post post = new Post();
        post.setAuthor(currentUser);
        post.setTitle(request.getTitle().trim());
        post.setSlug(generateUniqueSlug(request.getSlug(), request.getTitle()));
        post.setExcerpt(resolveExcerpt(request.getExcerpt(), request.getContent()));
        post.setContent(request.getContent().trim());

        PostStatus status = request.getStatus() == null ? PostStatus.PUBLISHED : request.getStatus();
        post.setStatus(status);
        if (status == PostStatus.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now());
        }
        post.setTags(resolveTags(request.getTags()));

        Post saved = postRepository.save(post);
        return toPostDetailResponse(saved, currentUser);
    }

    @Transactional
    public PostDetailResponse updatePost(Long postId, UpdatePostRequest request) {
        User currentUser = currentUserService.requireCurrentUser();

        Post post = findPost(postId);
        if (!canManagePost(post, currentUser)) {
            throw new BadRequestException("You are not allowed to edit this post");
        }

        post.setTitle(request.getTitle().trim());
        post.setSlug(generateUniqueSlug(request.getSlug(), request.getTitle(), post.getId()));
        post.setExcerpt(resolveExcerpt(request.getExcerpt(), request.getContent()));
        post.setContent(request.getContent().trim());

        PostStatus requestedStatus = request.getStatus() == null ? post.getStatus() : request.getStatus();
        post.setStatus(requestedStatus);
        if (requestedStatus == PostStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(LocalDateTime.now());
        }
        if (requestedStatus == PostStatus.DRAFT) {
            post.setPublishedAt(null);
        }

        post.setTags(resolveTags(request.getTags()));

        Post saved = postRepository.save(post);
        return toPostDetailResponse(saved, currentUser);
    }

    @Transactional
    public void deletePost(Long postId) {
        User currentUser = currentUserService.requireCurrentUser();

        Post post = findPost(postId);
        if (!canManagePost(post, currentUser)) {
            throw new BadRequestException("You are not allowed to delete this post");
        }
        postRepository.delete(post);
    }

    @Transactional
    public CommentResponse addComment(Long postId, CreateCommentRequest request) {
        User currentUser = currentUserService.getCurrentUserOptional().orElse(null);
        Post post = findReadablePost(postId, currentUser);

        // Comments are intentionally open to guests; logged-in users are linked to their account automatically.
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(currentUser);
        comment.setAuthorName(resolveCommentAuthor(request.getAuthor(), currentUser));
        comment.setText(request.getText().trim());

        Comment saved = commentRepository.save(comment);
        return toCommentResponse(saved);
    }

    @Transactional
    public LikeResponse incrementLike(Long postId) {
        User currentUser = currentUserService.requireCurrentUser();
        Post post = findReadablePost(postId, currentUser);

        // A user can like a post once. Calling the endpoint again toggles the existing like off.
        boolean exists = postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUser.getId());
        if (exists) {
            PostLike like = postLikeRepository.findByPostIdAndUserId(post.getId(), currentUser.getId())
                    .orElseThrow(() -> new NotFoundException("Like record not found"));
            postLikeRepository.delete(like);
        } else {
            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(currentUser);
            postLikeRepository.save(like);
        }

        int likeCount = (int) postLikeRepository.countByPostId(post.getId());
        boolean liked = postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUser.getId());
        return new LikeResponse(post.getId(), likeCount, liked);
    }

    @Transactional
    public ToggleResponse toggleBookmark(Long postId) {
        User currentUser = currentUserService.requireCurrentUser();
        Post post = findReadablePost(postId, currentUser);

        boolean exists = bookmarkRepository.existsByPostIdAndUserId(post.getId(), currentUser.getId());
        if (exists) {
            Bookmark bookmark = bookmarkRepository.findByPostIdAndUserId(post.getId(), currentUser.getId())
                    .orElseThrow(() -> new NotFoundException("Bookmark not found"));
            bookmarkRepository.delete(bookmark);
            return new ToggleResponse(post.getId(), false, "Bookmark removed");
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setPost(post);
        bookmark.setUser(currentUser);
        bookmarkRepository.save(bookmark);

        return new ToggleResponse(post.getId(), true, "Bookmark added");
    }

    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getBookmarkedPosts() {
        User currentUser = currentUserService.requireCurrentUser();

        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(Bookmark::getPost)
                .map(post -> toPostSummaryResponse(post, currentUser))
                .toList();
    }

    private String resolveCommentAuthor(String requestedAuthor, User currentUser) {
        if (currentUser != null && currentUser.getProfile() != null && currentUser.getProfile().getDisplayName() != null) {
            return currentUser.getProfile().getDisplayName();
        }
        if (currentUser != null) {
            return currentUser.getUsername();
        }
        if (requestedAuthor != null && !requestedAuthor.isBlank()) {
            return requestedAuthor.trim();
        }
        return "Guest";
    }

    private Post findReadablePost(Long postId, User currentUser) {
        Post post = findPost(postId);
        if (post.getStatus() == PostStatus.PUBLISHED) {
            return post;
        }
        if (currentUser == null) {
            throw new NotFoundException("Post not found: " + postId);
        }
        if (canManagePost(post, currentUser)) {
            return post;
        }
        throw new NotFoundException("Post not found: " + postId);
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found: " + postId));
    }

    private boolean canManagePost(Post post, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return true;
        }
        return post.getAuthor() != null && post.getAuthor().getId().equals(currentUser.getId());
    }

    private Set<Tag> resolveTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        return tagNames.stream()
                .map(this::normalizeTag)
                .filter(name -> !name.isBlank())
                .map(name -> tagRepository.findByName(name).orElseGet(() -> tagRepository.save(new Tag(name))))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private String normalizeTag(String tagName) {
        return tagName == null ? "" : tagName.trim().toLowerCase(Locale.ROOT);
    }

    private String generateUniqueSlug(String requestedSlug, String title) {
        return generateUniqueSlug(requestedSlug, title, null);
    }

    private String generateUniqueSlug(String requestedSlug, String title, Long existingPostId) {
        String base = (requestedSlug == null || requestedSlug.isBlank()) ? title : requestedSlug;
        String normalized = base.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (normalized.isBlank()) {
            normalized = "post";
        }

        String candidate = normalized;
        int suffix = 2;
        while (true) {
            Post sameSlugPost = postRepository.findBySlug(candidate).orElse(null);
            if (sameSlugPost == null) {
                return candidate;
            }
            if (existingPostId != null && sameSlugPost.getId().equals(existingPostId)) {
                return candidate;
            }
            candidate = normalized + "-" + suffix;
            suffix++;
        }
    }

    private String resolveExcerpt(String excerpt, String content) {
        if (excerpt != null && !excerpt.isBlank()) {
            return excerpt.trim();
        }
        String plain = content == null ? "" : content.trim();
        if (plain.length() <= 220) {
            return plain;
        }
        return plain.substring(0, 220).trim() + "...";
    }

    private PostSummaryResponse toPostSummaryResponse(Post post, User currentUser) {
        int likeCount = (int) postLikeRepository.countByPostId(post.getId());
        int commentCount = (int) commentRepository.countByPostId(post.getId());
        boolean liked = currentUser != null && postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUser.getId());
        boolean bookmarked = currentUser != null && bookmarkRepository.existsByPostIdAndUserId(post.getId(), currentUser.getId());

        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                resolveAuthorName(post),
                likeCount,
                post.getViewCount(),
                commentCount,
                post.getStatus(),
                post.getCreatedAt(),
                post.getPublishedAt(),
                post.getTags().stream().map(Tag::getName).collect(Collectors.toSet()),
                bookmarked,
                liked
        );
    }

    private PostDetailResponse toPostDetailResponse(Post post, User currentUser) {
        int likeCount = (int) postLikeRepository.countByPostId(post.getId());
        boolean liked = currentUser != null && postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUser.getId());
        boolean bookmarked = currentUser != null && bookmarkRepository.existsByPostIdAndUserId(post.getId(), currentUser.getId());

        List<CommentResponse> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(post.getId())
                .stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(this::toCommentResponse)
                .toList();

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getContent(),
                resolveAuthorName(post),
                likeCount,
                post.getViewCount(),
                post.getStatus(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getPublishedAt(),
                post.getTags().stream().map(Tag::getName).collect(Collectors.toSet()),
                comments,
                bookmarked,
                liked
        );
    }

    private String resolveAuthorName(Post post) {
        if (post.getAuthor() == null) {
            return "Guest";
        }
        if (post.getAuthor().getProfile() != null && post.getAuthor().getProfile().getDisplayName() != null) {
            return post.getAuthor().getProfile().getDisplayName();
        }
        return post.getAuthor().getUsername();
    }

    private CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getAuthorName(),
                comment.getText(),
                comment.getCreatedAt()
        );
    }
}
