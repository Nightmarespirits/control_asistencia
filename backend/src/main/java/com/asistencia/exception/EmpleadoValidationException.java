package com.asistencia.exception;

/**
 * Excepción personalizada para errores de validación específicos de empleados
 */
public class EmpleadoValidationException extends RuntimeException {
    
    public EmpleadoValidationException(String message) {
        super(message);
    }
    
    public EmpleadoValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}