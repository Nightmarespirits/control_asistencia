package com.asistencia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para logging estructurado de errores
 * Proporciona métodos para registrar diferentes tipos de errores con contexto adicional
 */
@Service
public class ErrorLoggingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorLoggingService.class);
    
    /**
     * Registra un error de autenticación
     */
    public void logAuthenticationError(String username, String ipAddress, String userAgent, String errorMessage) {
        try {
            MDC.put("event_type", "AUTHENTICATION_ERROR");
            MDC.put("username", username);
            MDC.put("ip_address", ipAddress);
            MDC.put("user_agent", userAgent);
            MDC.put("timestamp", LocalDateTime.now().toString());
            
            logger.warn("Authentication failed for user: {} from IP: {} - {}", username, ipAddress, errorMessage);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Registra un error de acceso denegado
     */
    public void logAccessDeniedError(String username, String resource, String method, String ipAddress) {
        try {
            MDC.put("event_type", "ACCESS_DENIED");
            MDC.put("username", username);
            MDC.put("resource", resource);
            MDC.put("method", method);
            MDC.put("ip_address", ipAddress);
            MDC.put("timestamp", LocalDateTime.now().toString());
            
            logger.warn("Access denied for user: {} trying to access: {} {} from IP: {}", 
                       username, method, resource, ipAddress);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Registra un error de validación de datos
     */
    public void logValidationError(String endpoint, String errorDetails, HttpServletRequest request) {
        try {
            MDC.put("event_type", "VALIDATION_ERROR");
            MDC.put("endpoint", endpoint);
            MDC.put("method", request.getMethod());
            MDC.put("ip_address", getClientIpAddress(request));
            MDC.put("user_agent", request.getHeader("User-Agent"));
            MDC.put("timestamp", LocalDateTime.now().toString());
            
            logger.warn("Validation error on {} {}: {}", request.getMethod(), endpoint, errorDetails);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Registra un error de integridad de datos
     */
    public void logDataIntegrityError(String operation, String entity, String errorDetails, HttpServletRequest request) {
        try {
            MDC.put("event_type", "DATA_INTEGRITY_ERROR");
            MDC.put("operation", operation);
            MDC.put("entity", entity);
            MDC.put("endpoint", request.getRequestURI());
            MDC.put("method", request.getMethod());
            MDC.put("ip_address", getClientIpAddress(request));
            MDC.put("timestamp", LocalDateTime.now().toString());
            
            logger.warn("Data integrity error during {} on {}: {}", operation, entity, errorDetails);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Registra un error interno del servidor
     */
    public void logInternalServerError(Exception exception, HttpServletRequest request) {
        try {
            MDC.put("event_type", "INTERNAL_SERVER_ERROR");
            MDC.put("endpoint", request.getRequestURI());
            MDC.put("method", request.getMethod());
            MDC.put("ip_address", getClientIpAddress(request));
            MDC.put("user_agent", request.getHeader("User-Agent"));
            MDC.put("exception_class", exception.getClass().getSimpleName());
            MDC.put("timestamp", LocalDateTime.now().toString());
            
            // Log request parameters (excluding sensitive data)
            Map<String, String> sanitizedParams = sanitizeRequestParameters(request);
            if (!sanitizedParams.isEmpty()) {
                MDC.put("request_params", sanitizedParams.toString());
            }
            
            logger.error("Internal server error on {} {}: {}", 
                        request.getMethod(), request.getRequestURI(), exception.getMessage(), exception);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Registra un error de marcación (específico del dominio)
     */
    public void logMarcacionError(String dni, String errorType, String errorMessage, String ipAddress) {
        try {
            MDC.put("event_type", "MARCACION_ERROR");
            MDC.put("dni", dni);
            MDC.put("error_type", errorType);
            MDC.put("ip_address", ipAddress);
            MDC.put("timestamp", LocalDateTime.now().toString());
            
            logger.warn("Marcación error for DNI: {} - Type: {} - Message: {}", dni, errorType, errorMessage);
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Registra actividad sospechosa
     */
    public void logSuspiciousActivity(String activity, String details, HttpServletRequest request) {
        try {
            MDC.put("event_type", "SUSPICIOUS_ACTIVITY");
            MDC.put("activity", activity);
            MDC.put("endpoint", request.getRequestURI());
            MDC.put("method", request.getMethod());
            MDC.put("ip_address", getClientIpAddress(request));
            MDC.put("user_agent", request.getHeader("User-Agent"));
            MDC.put("timestamp", LocalDateTime.now().toString());
            
            logger.warn("Suspicious activity detected: {} - Details: {} from IP: {}", 
                       activity, details, getClientIpAddress(request));
        } finally {
            MDC.clear();
        }
    }
    
    /**
     * Obtiene la dirección IP real del cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Sanitiza los parámetros de la request eliminando información sensible
     */
    private Map<String, String> sanitizeRequestParameters(HttpServletRequest request) {
        Map<String, String> sanitizedParams = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            
            // Excluir parámetros sensibles
            if (isSensitiveParameter(paramName)) {
                sanitizedParams.put(paramName, "[REDACTED]");
            } else {
                sanitizedParams.put(paramName, paramValue);
            }
        }
        
        return sanitizedParams;
    }
    
    /**
     * Determina si un parámetro contiene información sensible
     */
    private boolean isSensitiveParameter(String paramName) {
        String lowerParamName = paramName.toLowerCase();
        return lowerParamName.contains("password") || 
               lowerParamName.contains("token") || 
               lowerParamName.contains("secret") ||
               lowerParamName.contains("key");
    }
}