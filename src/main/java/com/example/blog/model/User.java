package com.example.blog.model;

import com.example.blog.model.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user")
    private Profile profile;

    @OneToMany(mappedBy = "author")
    private Set<Post> posts = new HashSet<>();

    @OneToMany(mappedBy = "author")
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<PostLike> likes = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Bookmark> bookmarks = new HashSet<>();

    @OneToMany(mappedBy = "follower")
    private Set<Follow> following = new HashSet<>();

    @OneToMany(mappedBy = "following")
    private Set<Follow> followers = new HashSet<>();

    public User() {
    }

    public User(String email, String username, String passwordHash, Role role) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Set<Post> getPosts() {
        return posts;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public Set<PostLike> getLikes() {
        return likes;
    }

    public Set<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public Set<Follow> getFollowing() {
        return following;
    }

    public Set<Follow> getFollowers() {
        return followers;
    }
}
