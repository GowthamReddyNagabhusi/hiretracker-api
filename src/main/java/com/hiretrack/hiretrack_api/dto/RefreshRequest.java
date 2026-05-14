package com.hiretrack.hiretrack_api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refreshing an expired access token.
 */
public class RefreshRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
