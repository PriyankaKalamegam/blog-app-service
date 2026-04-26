package com.example.blog.service;

import com.example.blog.dto.AuthLoginRequest;
import com.example.blog.dto.AuthRegisterRequest;
import com.example.blog.dto.AuthResponse;
import com.example.blog.dto.CurrentUserResponse;
import com.example.blog.exception.BadRequestException;
import com.example.blog.model.Profile;
import com.example.blog.model.User;
import com.example.blog.model.enums.Role;
import com.example.blog.repository.ProfileRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    public AuthService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            CurrentUserService currentUserService
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public AuthResponse register(AuthRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username is already taken");
        }

        User user = new User(
                request.email().trim().toLowerCase(),
                request.username().trim(),
                passwordEncoder.encode(request.password()),
                Role.USER
        );
        User savedUser = userRepository.save(user);

        Profile profile = new Profile();
        profile.setUser(savedUser);
        profile.setDisplayName(request.displayName().trim());
        profileRepository.save(profile);

        String token = jwtService.generateToken(savedUser);
        return new AuthResponse(token, jwtService.getExpirationSeconds(), toCurrentUser(savedUser, profile));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password())
        );

        User user = userRepository.findByUsername(request.identifier())
                .or(() -> userRepository.findByEmail(request.identifier()))
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, jwtService.getExpirationSeconds(), toCurrentUser(user, profile));
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse me() {
        User user = currentUserService.requireCurrentUser();
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        return toCurrentUser(user, profile);
    }

    private CurrentUserResponse toCurrentUser(User user, Profile profile) {
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                profile != null ? profile.getDisplayName() : user.getUsername(),
                profile != null ? profile.getAvatarUrl() : null
        );
    }
}
