package com.asistencia.controller;

import com.asistencia.dto.LoginRequestDTO;
import com.asistencia.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {
    
    @Autowired
    private AuthService authService;
    
    @Test
    void debePermitirLoginConCredencialesValidas() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin", "admin123");
        
        var response = authService.login(loginRequest);
        
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("admin", response.getUsername());
        assertNotNull(response.getExpiresIn());
    }
    
    @Test
    void debeRechazarLoginConCredencialesInvalidas() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("admin", "wrongpassword");
        
        assertThrows(Exception.class, () -> {
            authService.login(loginRequest);
        });
    }
}