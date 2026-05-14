package com.hiretrack.hiretrack_api.dto;

/**
 * Authentication response containing both access and refresh tokens.
 */
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String name;
    private String email;

    public AuthResponse(String token, String refreshToken, String name, String email) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.name = name;
        this.email = email;
    }

    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}