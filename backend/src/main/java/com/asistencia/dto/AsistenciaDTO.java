package com.asistencia.dto;

import com.asistencia.entity.EstadoMarcacion;
import com.asistencia.entity.TipoMarcacion;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class AsistenciaDTO {
    
    private Long id;
    
    @NotNull(message = "El ID del empleado es obligatorio")
    private Long empleadoId;
    
    private String empleadoNombre;
    
    private String empleadoDni;
    
    @NotNull(message = "La fecha y hora son obligatorias")
    private LocalDateTime fechaHora;
    
    @NotNull(message = "El tipo de marcación es obligatorio")
    private TipoMarcacion tipo;
    
    private EstadoMarcacion estado;
    
    private String observaciones;
    
    private LocalDateTime fechaCreacion;
    
    // Constructors
    public AsistenciaDTO() {}
    
    public AsistenciaDTO(Long empleadoId, LocalDateTime fechaHora, TipoMarcacion tipo) {
        this.empleadoId = empleadoId;
        this.fechaHora = fechaHora;
        this.tipo = tipo;
        this.estado = EstadoMarcacion.PUNTUAL;
    }
    
    public AsistenciaDTO(Long empleadoId, LocalDateTime fechaHora, TipoMarcacion tipo, EstadoMarcacion estado) {
        this.empleadoId = empleadoId;
        this.fechaHora = fechaHora;
        this.tipo = tipo;
        this.estado = estado;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getEmpleadoId() {
        return empleadoId;
    }
    
    public void setEmpleadoId(Long empleadoId) {
        this.empleadoId = empleadoId;
    }
    
    public String getEmpleadoNombre() {
        return empleadoNombre;
    }
    
    public void setEmpleadoNombre(String empleadoNombre) {
        this.empleadoNombre = empleadoNombre;
    }
    
    public String getEmpleadoDni() {
        return empleadoDni;
    }
    
    public void setEmpleadoDni(String empleadoDni) {
        this.empleadoDni = empleadoDni;
    }
    
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
    
    public TipoMarcacion getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoMarcacion tipo) {
        this.tipo = tipo;
    }
    
    public EstadoMarcacion getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoMarcacion estado) {
        this.estado = estado;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    @Override
    public String toString() {
        return "AsistenciaDTO{" +
                "id=" + id +
                ", empleadoId=" + empleadoId +
                ", empleadoNombre='" + empleadoNombre + '\'' +
                ", empleadoDni='" + empleadoDni + '\'' +
                ", fechaHora=" + fechaHora +
                ", tipo=" + tipo +
                ", estado=" + estado +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}