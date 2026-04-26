package com.example.blog.repository;

import com.example.blog.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByProfileIdOrderByFeaturedDescCreatedAtDesc(Long profileId);
}
