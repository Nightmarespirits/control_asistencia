package com.asistencia.service;

import com.asistencia.dto.LoginRequestDTO;
import com.asistencia.dto.LoginResponseDTO;
import com.asistencia.dto.RefreshTokenRequestDTO;
import com.asistencia.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Servicio de autenticación que maneja login y refresh de tokens JWT
 */
@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Value("${jwt.expiration:28800000}") // 8 horas en milisegundos
    private Long jwtExpiration;
    
    /**
     * Autentica un usuario y genera tokens JWT
     */
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        try {
            // Autenticar credenciales
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            // Cargar detalles del usuario
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            
            // Generar tokens
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            
            return new LoginResponseDTO(
                accessToken,
                refreshToken,
                jwtExpiration / 1000, // convertir a segundos
                userDetails.getUsername()
            );
            
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }
    
    /**
     * Refresca un access token usando un refresh token válido
     */
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        
        try {
            // Validar que sea un refresh token
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new BadCredentialsException("Token de refresh inválido");
            }
            
            // Extraer username del refresh token
            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // Validar el refresh token
            if (!jwtUtil.validateToken(refreshToken, userDetails)) {
                throw new BadCredentialsException("Token de refresh expirado o inválido");
            }
            
            // Generar nuevo access token
            String newAccessToken = jwtUtil.generateToken(userDetails);
            
            return new LoginResponseDTO(
                newAccessToken,
                refreshToken, // mantener el mismo refresh token
                jwtExpiration / 1000,
                userDetails.getUsername()
            );
            
        } catch (Exception e) {
            throw new BadCredentialsException("Error al procesar el token de refresh: " + e.getMessage());
        }
    }
}