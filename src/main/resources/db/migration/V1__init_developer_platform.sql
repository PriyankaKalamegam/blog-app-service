CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(160) NOT NULL UNIQUE,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    display_name VARCHAR(120) NOT NULL,
    headline VARCHAR(220),
    bio VARCHAR(2500),
    avatar_url VARCHAR(500),
    github_username VARCHAR(120),
    website_url VARCHAR(500),
    location VARCHAR(120),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE profile_skills (
    profile_id BIGINT NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    skill VARCHAR(80) NOT NULL,
    PRIMARY KEY (profile_id, skill)
);

CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    author_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(180) NOT NULL,
    slug VARCHAR(220) NOT NULL UNIQUE,
    excerpt VARCHAR(360),
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    view_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP
);

CREATE INDEX idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX idx_posts_published_at ON posts(published_at DESC);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    author_name VARCHAR(120) NOT NULL,
    text VARCHAR(1200) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_comments_post_id ON comments(post_id);

CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE post_tags (
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

CREATE TABLE post_likes (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_post_like UNIQUE (post_id, user_id)
);

CREATE INDEX idx_post_likes_post_id ON post_likes(post_id);

CREATE TABLE bookmarks (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_bookmark UNIQUE (post_id, user_id)
);

CREATE TABLE follows (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_follow UNIQUE (follower_id, following_id)
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(2000),
    repository_url VARCHAR(500),
    live_url VARCHAR(500),
    tech_stack VARCHAR(500),
    featured BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE resumes (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL UNIQUE REFERENCES profiles(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(700) NOT NULL,
    summary VARCHAR(2000),
    updated_at TIMESTAMP NOT NULL
);
