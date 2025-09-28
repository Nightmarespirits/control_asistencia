package com.asistencia.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para peticiones de refresh token
 */
public class RefreshTokenRequestDTO {
    
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
    
    // Constructors
    public RefreshTokenRequestDTO() {}
    
    public RefreshTokenRequestDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Getters and Setters
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    @Override
    public String toString() {
        return "RefreshTokenRequestDTO{" +
                "refreshToken='[PROTECTED]'" +
                '}';
    }
}