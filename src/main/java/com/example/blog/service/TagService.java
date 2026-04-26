package com.example.blog.service;

import com.example.blog.dto.TagResponse;
import com.example.blog.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .sorted(Comparator.comparing(tag -> tag.getName().toLowerCase()))
                .map(tag -> new TagResponse(tag.getId(), tag.getName(), tag.getPosts().size()))
                .toList();
    }
}
