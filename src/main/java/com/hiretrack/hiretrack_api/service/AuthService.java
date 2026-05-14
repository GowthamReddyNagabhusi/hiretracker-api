package com.hiretrack.hiretrack_api.service;

import com.hiretrack.hiretrack_api.dto.AuthResponse;
import com.hiretrack.hiretrack_api.dto.LoginRequest;
import com.hiretrack.hiretrack_api.dto.RefreshRequest;
import com.hiretrack.hiretrack_api.dto.RegisterRequest;
import com.hiretrack.hiretrack_api.exception.DuplicateResourceException;
import com.hiretrack.hiretrack_api.exception.ResourceNotFoundException;
import com.hiretrack.hiretrack_api.model.RefreshToken;
import com.hiretrack.hiretrack_api.model.User;
import com.hiretrack.hiretrack_api.repository.UserRepository;
import com.hiretrack.hiretrack_api.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        String accessToken = jwtUtil.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getName(), user.getEmail());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("User logged in: {}", user.getEmail());

        String accessToken = jwtUtil.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getName(), user.getEmail());
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken oldToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid refresh token"));

        // Rotate the refresh token (revokes old, creates new)
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(oldToken);

        // Issue new access token
        User user = newRefreshToken.getUser();
        String accessToken = jwtUtil.generateToken(user.getEmail());

        log.info("Token refreshed for user: {}", user.getEmail());

        return new AuthResponse(accessToken, newRefreshToken.getToken(), user.getName(), user.getEmail());
    }

    @Transactional
    public void logout() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenService.revokeAllTokens(user);
        log.info("User logged out (all tokens revoked): {}", email);
    }
}