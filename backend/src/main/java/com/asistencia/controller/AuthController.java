package com.asistencia.controller;

import com.asistencia.dto.LoginRequestDTO;
import com.asistencia.dto.LoginResponseDTO;
import com.asistencia.dto.RefreshTokenRequestDTO;
import com.asistencia.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para endpoints de autenticación
 * Maneja login y refresh de tokens JWT
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * Endpoint para autenticación de usuarios
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseDTO response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "INVALID_CREDENTIALS");
            error.put("message", "Usuario o contraseña incorrectos");
            return ResponseEntity.status(401).body(error);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "AUTHENTICATION_ERROR");
            error.put("message", "Error en el proceso de autenticación");
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Endpoint para refrescar tokens de acceso
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        try {
            LoginResponseDTO response = authService.refreshToken(refreshRequest);
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "INVALID_REFRESH_TOKEN");
            error.put("message", "Token de refresh inválido o expirado");
            return ResponseEntity.status(401).body(error);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "REFRESH_ERROR");
            error.put("message", "Error al refrescar el token");
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Endpoint para validar si un token es válido
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken() {
        // Si llega hasta aquí, el token es válido (filtro JWT ya lo validó)
        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("message", "Token válido");
        return ResponseEntity.ok(response);
    }
}