package com.example.blog.config;

import com.example.blog.model.Comment;
import com.example.blog.model.Post;
import com.example.blog.model.PostLike;
import com.example.blog.model.Profile;
import com.example.blog.model.Tag;
import com.example.blog.model.User;
import com.example.blog.model.enums.PostStatus;
import com.example.blog.model.enums.Role;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostLikeRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.ProfileRepository;
import com.example.blog.repository.TagRepository;
import com.example.blog.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final TagRepository tagRepository;
    private final PostLikeRepository postLikeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            TagRepository tagRepository,
            PostLikeRepository postLikeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.tagRepository = tagRepository;
        this.postLikeRepository = postLikeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = userRepository.save(new User(
                "admin@devblog.io",
                "admin",
                passwordEncoder.encode("Admin@123"),
                Role.ADMIN
        ));

        User priyanka = userRepository.save(new User(
                "priyanka@devblog.io",
                "priyanka",
                passwordEncoder.encode("Priyanka@123"),
                Role.USER
        ));

        Profile adminProfile = new Profile();
        adminProfile.setUser(admin);
        adminProfile.setDisplayName("Platform Admin");
        adminProfile.setHeadline("Maintaining quality across developer stories");
        adminProfile.setBio("Admin profile for moderation and platform operations.");
        adminProfile.setSkills(Set.of("Spring Boot", "Security", "PostgreSQL"));
        profileRepository.save(adminProfile);

        Profile priyankaProfile = new Profile();
        priyankaProfile.setUser(priyanka);
        priyankaProfile.setDisplayName("Priyanka Kalamegam");
        priyankaProfile.setHeadline("Java + React Engineer");
        priyankaProfile.setBio("Building practical full-stack systems with strong developer UX.");
        priyankaProfile.setGithubUsername("octocat");
        priyankaProfile.setSkills(Set.of("Java", "React", "Spring Boot", "Tailwind"));
        profileRepository.save(priyankaProfile);

        Tag javaTag = saveTagIfMissing("java");
        Tag springTag = saveTagIfMissing("spring-boot");
        Tag reactTag = saveTagIfMissing("react");
        Tag postgresTag = saveTagIfMissing("postgresql");

        Post post1 = new Post();
        post1.setAuthor(priyanka);
        post1.setTitle("From Monolith to Developer Platform");
        post1.setSlug("from-monolith-to-developer-platform");
        post1.setExcerpt("A practical migration path from a simple demo app to a production-style developer blog platform.");
        post1.setContent("## Migration Notes\n\nWe started by separating auth, profile, and content domains while keeping existing API routes stable.");
        post1.setStatus(PostStatus.PUBLISHED);
        post1.setPublishedAt(LocalDateTime.now().minusDays(2));
        post1.setViewCount(128);
        post1.setTags(Set.of(javaTag, springTag, postgresTag));
        Post savedPost1 = postRepository.save(post1);

        Post post2 = new Post();
        post2.setAuthor(priyanka);
        post2.setTitle("React UI Refresh for Technical Writing");
        post2.setSlug("react-ui-refresh-for-technical-writing");
        post2.setExcerpt("Designing a cleaner writing and reading experience with reusable React modules.");
        post2.setContent("## Frontend Revamp\n\nWe split the app into routes, reusable components, and service adapters.");
        post2.setStatus(PostStatus.PUBLISHED);
        post2.setPublishedAt(LocalDateTime.now().minusDays(1));
        post2.setViewCount(94);
        post2.setTags(Set.of(reactTag, springTag));
        Post savedPost2 = postRepository.save(post2);

        Comment comment1 = new Comment();
        comment1.setPost(savedPost1);
        comment1.setAuthor(admin);
        comment1.setAuthorName("Platform Admin");
        comment1.setText("Great migration story. Please add security hardening notes next.");
        commentRepository.save(comment1);

        Comment comment2 = new Comment();
        comment2.setPost(savedPost2);
        comment2.setAuthorName("Ravi");
        comment2.setText("The component split makes the UI much easier to reason about.");
        commentRepository.save(comment2);

        PostLike like = new PostLike();
        like.setPost(savedPost1);
        like.setUser(admin);
        postLikeRepository.save(like);
    }

    private Tag saveTagIfMissing(String name) {
        return tagRepository.findByName(name).orElseGet(() -> tagRepository.save(new Tag(name)));
    }
}
