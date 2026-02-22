package com.example.DocsSignatureAppBE.Service;

import com.example.DocsSignatureAppBE.Entity.Role;
import com.example.DocsSignatureAppBE.Entity.User;
import com.example.DocsSignatureAppBE.Security.JwtTokenProvider;
import com.example.DocsSignatureAppBE.DTO.AuthRequest;
import com.example.DocsSignatureAppBE.DTO.AuthResponse;
import com.example.DocsSignatureAppBE.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse register(@NotNull AuthRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);

        String token = tokenProvider.generateTokenWithRole(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getRole().getAuthority());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = tokenProvider.generateTokenWithRole(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getRole().getAuthority());
    }

    public User changeUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(newRole);
        return userRepository.save(user);
    }
}
