package com.asistencia.exception;

/**
 * Excepción lanzada cuando no se encuentra un empleado
 */
public class EmpleadoNotFoundException extends RuntimeException {
    
    public EmpleadoNotFoundException(String message) {
        super(message);
    }
    
    public EmpleadoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static EmpleadoNotFoundException porDni(String dni) {
        return new EmpleadoNotFoundException("Empleado no encontrado con DNI: " + dni);
    }
}