package com.asistencia.entity;

public enum EstadoMarcacion {
    PUNTUAL("Puntual"),
    TARDANZA("Tardanza"),
    FUERA_HORARIO("Fuera de horario");
    
    private final String descripcion;
    
    EstadoMarcacion(String descripcion) {
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