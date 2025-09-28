package com.asistencia.entity;

public enum TipoMarcacion {
    ENTRADA("Entrada"),
    SALIDA_ALMUERZO("Salida a almuerzo"),
    RETORNO_ALMUERZO("Retorno de almuerzo"),
    SALIDA("Salida"),
    FUERA_HORARIO("Fuera de horario");
    
    private final String descripcion;
    
    TipoMarcacion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    @Override
    public String toString() {
        return descripcion;
    }
}