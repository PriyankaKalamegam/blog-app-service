package com.example.blog.repository;

import com.example.blog.model.Post;
import com.example.blog.model.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByCreatedAtDesc();

    List<Post> findAllByStatusOrderByCreatedAtDesc(PostStatus status);

    Optional<Post> findByIdAndStatus(Long id, PostStatus status);

    List<Post> findByAuthorUsernameOrderByCreatedAtDesc(String username);

    List<Post> findByAuthorIdOrderByUpdatedAtDesc(Long authorId);

    boolean existsBySlug(String slug);

    Optional<Post> findBySlug(String slug);

    long countByAuthorId(Long authorId);

    long countByAuthorIdAndStatus(Long authorId, PostStatus status);

    @Query("""
            select p from Post p
            where p.status = :status
            and (
                lower(p.title) like lower(concat('%', :term, '%'))
                or lower(coalesce(p.excerpt, '')) like lower(concat('%', :term, '%'))
            )
            order by p.publishedAt desc nulls last, p.createdAt desc
            """)
    List<Post> searchPublishedByTerm(@Param("status") PostStatus status, @Param("term") String term);
}
