package com.airops.handling.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String username,
        String role
) {
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, String username, String role) {
        this(accessToken, refreshToken, "Bearer", expiresIn, username, role);
    }
}
