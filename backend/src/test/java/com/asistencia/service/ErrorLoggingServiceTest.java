package com.asistencia.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorLoggingServiceTest {

    private ErrorLoggingService errorLoggingService;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        errorLoggingService = new ErrorLoggingService();
        
        // Setup logback test appender
        logger = (Logger) LoggerFactory.getLogger(ErrorLoggingService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void logAuthenticationError_ShouldLogWithCorrectFormat() {
        // Given
        String username = "testuser";
        String ipAddress = "192.168.1.100";
        String userAgent = "Mozilla/5.0 Test Browser";
        String errorMessage = "Invalid credentials";

        // When
        errorLoggingService.logAuthenticationError(username, ipAddress, userAgent, errorMessage);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getMessage()).contains("Authentication failed for user: {} from IP: {} - {}");
        assertThat(logEvent.getArgumentArray()).containsExactly(username, ipAddress, errorMessage);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("event_type", "AUTHENTICATION_ERROR");
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("username", username);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("ip_address", ipAddress);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("user_agent", userAgent);
    }

    @Test
    void logAccessDeniedError_ShouldLogWithCorrectFormat() {
        // Given
        String username = "testuser";
        String resource = "/api/admin/empleados";
        String method = "POST";
        String ipAddress = "192.168.1.100";

        // When
        errorLoggingService.logAccessDeniedError(username, resource, method, ipAddress);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getMessage()).contains("Access denied for user: {} trying to access: {} {} from IP: {}");
        assertThat(logEvent.getArgumentArray()).containsExactly(username, method, resource, ipAddress);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("event_type", "ACCESS_DENIED");
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("username", username);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("resource", resource);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("method", method);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("ip_address", ipAddress);
    }

    @Test
    void logValidationError_ShouldLogWithCorrectFormat() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/empleados");
        request.setMethod("POST");
        request.setRemoteAddr("192.168.1.100");
        request.addHeader("User-Agent", "Mozilla/5.0 Test Browser");
        
        String endpoint = "/api/admin/empleados";
        String errorDetails = "DNI is required";

        // When
        errorLoggingService.logValidationError(endpoint, errorDetails, request);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getMessage()).contains("Validation error on {} {}: {}");
        assertThat(logEvent.getArgumentArray()).containsExactly("POST", endpoint, errorDetails);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("event_type", "VALIDATION_ERROR");
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("endpoint", endpoint);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("method", "POST");
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("ip_address", "192.168.1.100");
    }

    @Test
    void logMarcacionError_ShouldLogWithCorrectFormat() {
        // Given
        String dni = "12345678";
        String errorType = "EMPLEADO_NO_ENCONTRADO";
        String errorMessage = "Employee not found";
        String ipAddress = "192.168.1.100";

        // When
        errorLoggingService.logMarcacionError(dni, errorType, errorMessage, ipAddress);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getMessage()).contains("Marcación error for DNI: {} - Type: {} - Message: {}");
        assertThat(logEvent.getArgumentArray()).containsExactly(dni, errorType, errorMessage);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("event_type", "MARCACION_ERROR");
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("dni", dni);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("error_type", errorType);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("ip_address", ipAddress);
    }

    @Test
    void logInternalServerError_ShouldLogWithCorrectFormat() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/empleados");
        request.setMethod("POST");
        request.setRemoteAddr("192.168.1.100");
        request.addHeader("User-Agent", "Mozilla/5.0 Test Browser");
        request.setParameter("nombre", "John");
        request.setParameter("password", "secret123"); // This should be redacted
        
        RuntimeException exception = new RuntimeException("Database connection failed");

        // When
        errorLoggingService.logInternalServerError(exception, request);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getMessage()).contains("Internal server error on {} {}: {}");
        assertThat(logEvent.getArgumentArray()).containsExactly("POST", "/api/admin/empleados", "Database connection failed");
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("event_type", "INTERNAL_SERVER_ERROR");
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("endpoint", "/api/admin/empleados");
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("method", "POST");
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("exception_class", "RuntimeException");
        
        // Check that sensitive parameters are redacted
        String requestParams = logEvent.getMDCPropertyMap().get("request_params");
        assertThat(requestParams).contains("nombre=John");
        assertThat(requestParams).contains("password=[REDACTED]");
    }

    @Test
    void logSuspiciousActivity_ShouldLogWithCorrectFormat() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/empleados");
        request.setMethod("POST");
        request.setRemoteAddr("192.168.1.100");
        request.addHeader("User-Agent", "Mozilla/5.0 Test Browser");
        
        String activity = "Multiple failed login attempts";
        String details = "5 failed attempts in 1 minute";

        // When
        errorLoggingService.logSuspiciousActivity(activity, details, request);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getMessage()).contains("Suspicious activity detected: {} - Details: {} from IP: {}");
        assertThat(logEvent.getArgumentArray()).containsExactly(activity, details, "192.168.1.100");
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("event_type", "SUSPICIOUS_ACTIVITY");
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("activity", activity);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("ip_address", "192.168.1.100");
    }

    @Test
    void getClientIpAddress_ShouldExtractFromXForwardedFor() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");
        request.setRemoteAddr("192.168.1.1");

        // When
        errorLoggingService.logValidationError("/test", "test error", request);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("ip_address", "203.0.113.195");
    }

    @Test
    void getClientIpAddress_ShouldExtractFromXRealIp() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "203.0.113.195");
        request.setRemoteAddr("192.168.1.1");

        // When
        errorLoggingService.logValidationError("/test", "test error", request);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("ip_address", "203.0.113.195");
    }

    @Test
    void getClientIpAddress_ShouldFallbackToRemoteAddr() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.100");

        // When
        errorLoggingService.logValidationError("/test", "test error", request);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getMDCPropertyMap()).containsEntry("ip_address", "192.168.1.100");
    }
}