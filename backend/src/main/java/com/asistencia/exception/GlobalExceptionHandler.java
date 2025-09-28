package com.asistencia.exception;

import com.asistencia.dto.MarcacionResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la aplicación
 * Proporciona respuestas consistentes para diferentes tipos de errores
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final com.asistencia.service.ErrorLoggingService errorLoggingService;
    
    public GlobalExceptionHandler(com.asistencia.service.ErrorLoggingService errorLoggingService) {
        this.errorLoggingService = errorLoggingService;
    }
    
    /**
     * Maneja excepciones cuando no se encuentra un empleado
     */
    @ExceptionHandler(EmpleadoNotFoundException.class)
    public ResponseEntity<MarcacionResponseDTO> handleEmpleadoNotFound(EmpleadoNotFoundException ex, HttpServletRequest request) {
        String dni = extractDniFromException(ex.getMessage());
        errorLoggingService.logMarcacionError(dni, "EMPLEADO_NO_ENCONTRADO", ex.getMessage(), 
                                            getClientIpAddress(request));
        MarcacionResponseDTO response = MarcacionResponseDTO.error("Empleado no encontrado");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Maneja excepciones de marcación duplicada
     */
    @ExceptionHandler(MarcacionDuplicadaException.class)
    public ResponseEntity<MarcacionResponseDTO> handleMarcacionDuplicada(MarcacionDuplicadaException ex, HttpServletRequest request) {
        String dni = extractDniFromException(ex.getMessage());
        errorLoggingService.logMarcacionError(dni, "MARCACION_DUPLICADA", ex.getMessage(), 
                                            getClientIpAddress(request));
        MarcacionResponseDTO response = MarcacionResponseDTO.error("Ya existe una marcación reciente");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Maneja errores de validación de campos
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        response.put("success", false);
        response.put("mensaje", "Error de validación");
        response.put("errores", errors);
        
        errorLoggingService.logValidationError(request.getRequestURI(), errors.toString(), request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Maneja errores de validación específicos de empleados
     */
    @ExceptionHandler(EmpleadoValidationException.class)
    public ResponseEntity<Map<String, Object>> handleEmpleadoValidation(EmpleadoValidationException ex, HttpServletRequest request) {
        errorLoggingService.logValidationError(request.getRequestURI(), ex.getMessage(), request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Maneja argumentos ilegales
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MarcacionResponseDTO> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        errorLoggingService.logValidationError(request.getRequestURI(), ex.getMessage(), request);
        MarcacionResponseDTO response = MarcacionResponseDTO.error("Datos inválidos: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Maneja errores de autenticación
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        String username = request.getParameter("username");
        errorLoggingService.logAuthenticationError(username != null ? username : "unknown", 
                                                 getClientIpAddress(request), 
                                                 request.getHeader("User-Agent"), 
                                                 ex.getMessage());
        Map<String, Object> response = createErrorResponse("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Maneja errores de credenciales incorrectas
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        String username = request.getParameter("username");
        errorLoggingService.logAuthenticationError(username != null ? username : "unknown", 
                                                 getClientIpAddress(request), 
                                                 request.getHeader("User-Agent"), 
                                                 "Bad credentials");
        Map<String, Object> response = createErrorResponse("Usuario o contraseña incorrectos", HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Maneja errores de acceso denegado
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
        errorLoggingService.logAccessDeniedError(username, request.getRequestURI(), 
                                               request.getMethod(), getClientIpAddress(request));
        Map<String, Object> response = createErrorResponse("No tienes permisos para acceder a este recurso", HttpStatus.FORBIDDEN);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * Maneja errores de integridad de datos (duplicados, constraints)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "Error de integridad de datos";
        String entity = "unknown";
        
        // Personalizar mensaje según el tipo de constraint
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("dni")) {
                message = "Ya existe un empleado con este DNI";
                entity = "empleado";
            } else if (ex.getMessage().contains("codigo_unico")) {
                message = "Ya existe un empleado con este código";
                entity = "empleado";
            } else if (ex.getMessage().contains("username")) {
                message = "Ya existe un usuario con este nombre";
                entity = "usuario";
            }
        }
        
        errorLoggingService.logDataIntegrityError(request.getMethod(), entity, ex.getMessage(), request);
        Map<String, Object> response = createErrorResponse(message, HttpStatus.CONFLICT);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Maneja errores de parámetros faltantes
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        logger.warn("Parámetro faltante en {}: {}", request.getRequestURI(), ex.getMessage());
        String message = String.format("Parámetro requerido '%s' no encontrado", ex.getParameterName());
        Map<String, Object> response = createErrorResponse(message, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Maneja errores de tipo de argumento incorrecto
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        logger.warn("Tipo de argumento incorrecto en {}: {}", request.getRequestURI(), ex.getMessage());
        String message = String.format("Valor inválido para el parámetro '%s'", ex.getName());
        Map<String, Object> response = createErrorResponse(message, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Maneja errores de método HTTP no soportado
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        logger.warn("Método HTTP no soportado en {}: {}", request.getRequestURI(), ex.getMessage());
        String message = String.format("Método %s no soportado para esta URL", ex.getMethod());
        Map<String, Object> response = createErrorResponse(message, HttpStatus.METHOD_NOT_ALLOWED);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }
    
    /**
     * Maneja errores de JSON malformado
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.warn("JSON malformado en {}: {}", request.getRequestURI(), ex.getMessage());
        Map<String, Object> response = createErrorResponse("Formato de datos inválido", HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Maneja errores 404 - Endpoint no encontrado
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        logger.warn("Endpoint no encontrado: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        Map<String, Object> response = createErrorResponse("Endpoint no encontrado", HttpStatus.NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Maneja cualquier otra excepción no específica
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        errorLoggingService.logInternalServerError(ex, request);
        Map<String, Object> response = createErrorResponse("Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Crea una respuesta de error estándar
     */
    private Map<String, Object> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("mensaje", message);
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        return response;
    }
    
    /**
     * Extrae el DNI del mensaje de excepción
     */
    private String extractDniFromException(String message) {
        // Intenta extraer el DNI del mensaje de error
        if (message != null && message.contains("DNI")) {
            String[] parts = message.split("DNI");
            if (parts.length > 1) {
                String dniPart = parts[1].trim();
                // Extrae los primeros 8 dígitos
                return dniPart.replaceAll("[^0-9]", "").substring(0, Math.min(8, dniPart.length()));
            }
        }
        return "unknown";
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
}