package com.asistencia.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    private UserDetails userDetails;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Configurar propiedades usando ReflectionTestUtils
        ReflectionTestUtils.setField(jwtUtil, "secret", "myTestSecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hora
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 86400000L); // 1 día
        
        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
    }
    
    @Test
    void debeGenerarTokenJWT() {
        String token = jwtUtil.generateToken(userDetails);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tiene 3 partes separadas por puntos
    }
    
    @Test
    void debeExtraerUsernameDelToken() {
        String token = jwtUtil.generateToken(userDetails);
        String username = jwtUtil.extractUsername(token);
        
        assertEquals("testuser", username);
    }
    
    @Test
    void debeValidarTokenCorrectamente() {
        String token = jwtUtil.generateToken(userDetails);
        
        assertTrue(jwtUtil.validateToken(token, userDetails));
        assertTrue(jwtUtil.validateToken(token));
    }
    
    @Test
    void debeGenerarRefreshToken() {
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        
        assertNotNull(refreshToken);
        assertTrue(jwtUtil.isRefreshToken(refreshToken));
        assertEquals("testuser", jwtUtil.extractUsername(refreshToken));
    }
    
    @Test
    void debeDistinguirEntreAccessYRefreshToken() {
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        
        assertFalse(jwtUtil.isRefreshToken(accessToken));
        assertTrue(jwtUtil.isRefreshToken(refreshToken));
    }
    
    @Test
    void debeRechazarTokenInvalido() {
        String invalidToken = "invalid.token.here";
        
        assertFalse(jwtUtil.validateToken(invalidToken));
        assertFalse(jwtUtil.isRefreshToken(invalidToken));
    }
    
    @Test
    void debeExtraerFechaExpiracion() {
        String token = jwtUtil.generateToken(userDetails);
        
        assertNotNull(jwtUtil.extractExpiration(token));
        assertTrue(jwtUtil.extractExpiration(token).getTime() > System.currentTimeMillis());
    }
}