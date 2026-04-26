package com.example.blog.repository;

import com.example.blog.model.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    Optional<Bookmark> findByPostIdAndUserId(Long postId, Long userId);

    List<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId);
}
