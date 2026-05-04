package com.airops.handling.service;

import com.airops.handling.dto.request.LoginRequest;
import com.airops.handling.dto.response.AuthResponse;
import com.airops.handling.repository.UserRepository;
import com.airops.handling.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepo;

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        String accessToken  = jwtProvider.generateToken(auth.getName());
        String refreshToken = jwtProvider.generateRefreshToken(auth.getName());

        var user = userRepo.findByUsername(req.username())
                .orElseThrow(() -> new UsernameNotFoundException(req.username()));

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtProvider.getExpirationMs(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    public AuthResponse refresh(String refreshToken) {
        jwtProvider.validateToken(refreshToken);
        String username = jwtProvider.getUsernameFromToken(refreshToken);

        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        String newAccess  = jwtProvider.generateToken(username);
        String newRefresh = jwtProvider.generateRefreshToken(username);

        return new AuthResponse(
                newAccess,
                newRefresh,
                jwtProvider.getExpirationMs(),
                user.getUsername(),
                user.getRole().name()
        );
    }
}
