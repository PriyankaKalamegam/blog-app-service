package com.example.blog.service;

import com.example.blog.exception.UnauthorizedException;
import com.example.blog.model.User;
import com.example.blog.repository.UserRepository;
import com.example.blog.security.AppUserPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        // Support the custom JWT principal plus Spring's default principal shapes for easier testing.
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserPrincipal appUserPrincipal) {
            Long userId = appUserPrincipal.getUser().getId();
            return userRepository.findById(userId);
        }
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            return userRepository.findByUsername(userDetails.getUsername());
        }
        if (principal instanceof String username) {
            return userRepository.findByUsername(username);
        }

        return Optional.empty();
    }

    public User requireCurrentUser() {
        // Use this helper when a service method cannot safely run for anonymous visitors.
        return getCurrentUserOptional().orElseThrow(() -> new UnauthorizedException("Authentication required"));
    }
}
