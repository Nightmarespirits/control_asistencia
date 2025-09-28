package com.asistencia.exception;

import com.asistencia.service.ErrorLoggingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private ErrorLoggingService errorLoggingService;

    @Mock
    private BindingResult bindingResult;

    private GlobalExceptionHandler globalExceptionHandler;
    private MockHttpServletRequest request;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler(errorLoggingService);
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        request.setMethod("POST");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "Mozilla/5.0 Test Browser");
        objectMapper = new ObjectMapper();
    }

    @Test
    void handleEmpleadoNotFound_ShouldReturnNotFoundResponse() {
        // Given
        EmpleadoNotFoundException exception = new EmpleadoNotFoundException("Empleado con DNI 12345678 no encontrado");

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleEmpleadoNotFound(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(errorLoggingService).logMarcacionError(anyString(), eq("EMPLEADO_NO_ENCONTRADO"), anyString(), anyString());
    }

    @Test
    void handleMarcacionDuplicada_ShouldReturnConflictResponse() {
        // Given
        MarcacionDuplicadaException exception = new MarcacionDuplicadaException("Marcación duplicada para DNI 12345678");

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleMarcacionDuplicada(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(errorLoggingService).logMarcacionError(anyString(), eq("MARCACION_DUPLICADA"), anyString(), anyString());
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequestWithErrors() {
        // Given
        FieldError fieldError = new FieldError("empleado", "dni", "DNI es requerido");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationExceptions(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("mensaje")).isEqualTo("Error de validación");
        
        @SuppressWarnings("unchecked")
        Map<String, String> errores = (Map<String, String>) response.getBody().get("errores");
        assertThat(errores).containsEntry("dni", "DNI es requerido");
        
        verify(errorLoggingService).logValidationError(anyString(), anyString(), any());
    }

    @Test
    void handleBadCredentials_ShouldReturnUnauthorizedResponse() {
        // Given
        request.setParameter("username", "testuser");
        request.addHeader("User-Agent", "Mozilla/5.0 Test Browser");
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleBadCredentials(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("mensaje")).isEqualTo("Usuario o contraseña incorrectos");
        
        verify(errorLoggingService).logAuthenticationError(eq("testuser"), anyString(), anyString(), eq("Bad credentials"));
    }

    @Test
    void handleAccessDenied_ShouldReturnForbiddenResponse() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleAccessDenied(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("mensaje")).isEqualTo("No tienes permisos para acceder a este recurso");
        
        verify(errorLoggingService).logAccessDeniedError(eq("anonymous"), anyString(), anyString(), anyString());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerErrorResponse() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGenericException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("mensaje")).isEqualTo("Error interno del servidor");
        
        verify(errorLoggingService).logInternalServerError(exception, request);
    }

    @Test
    void createErrorResponse_ShouldCreateStandardErrorFormat() {
        // Given
        String message = "Test error message";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // When
        // We can't test this directly as it's private, but we can test it through other methods
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGenericException(
            new RuntimeException(message), request);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKeys("success", "mensaje", "timestamp", "status");
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}