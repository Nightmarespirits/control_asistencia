package com.asistencia.exception;

/**
 * Excepción lanzada cuando se intenta registrar una marcación duplicada
 */
public class MarcacionDuplicadaException extends RuntimeException {
    
    public MarcacionDuplicadaException(String message) {
        super(message);
    }
    
    public MarcacionDuplicadaException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public MarcacionDuplicadaException(String dni, String tipoMarcacion) {
        super("Ya existe una marcación reciente de tipo " + tipoMarcacion + " para el empleado con DNI: " + dni);
    }
}