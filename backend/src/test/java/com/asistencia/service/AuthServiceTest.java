package com.asistencia.service;

import com.asistencia.dto.LoginRequestDTO;
import com.asistencia.dto.LoginResponseDTO;
import com.asistencia.entity.Usuario;
import com.asistencia.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setUp() {
        // Crear usuario de prueba
        Usuario testUser = new Usuario();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("testpass"));
        testUser.setEmail("test@example.com");
        testUser.setActivo(true);
        usuarioRepository.save(testUser);
    }
    
    @Test
    void debeAutenticarUsuarioConCredencialesValidas() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "testpass");
        
        LoginResponseDTO response = authService.login(loginRequest);
        
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("testuser", response.getUsername());
        assertTrue(response.getExpiresIn() > 0);
    }
    
    @Test
    void debeRechazarCredencialesInvalidas() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "wrongpass");
        
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }
    
    @Test
    void debeRechazarUsuarioInexistente() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("nonexistent", "password");
        
        assertThrows(Exception.class, () -> {
            authService.login(loginRequest);
        });
    }
}