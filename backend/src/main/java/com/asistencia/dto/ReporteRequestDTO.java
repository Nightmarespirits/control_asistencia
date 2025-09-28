package com.asistencia.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ReporteRequestDTO {
    
    @NotNull(message = "La fecha de inicio es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInicio;
    
    @NotNull(message = "La fecha de fin es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;
    
    private Long empleadoId;
    
    private String tipoMarcacion;
    
    // Constructors
    public ReporteRequestDTO() {}
    
    public ReporteRequestDTO(LocalDate fechaInicio, LocalDate fechaFin, Long empleadoId, String tipoMarcacion) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.empleadoId = empleadoId;
        this.tipoMarcacion = tipoMarcacion;
    }
    
    // Getters and Setters
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }
    
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    public LocalDate getFechaFin() {
        return fechaFin;
    }
    
    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
    
    public Long getEmpleadoId() {
        return empleadoId;
    }
    
    public void setEmpleadoId(Long empleadoId) {
        this.empleadoId = empleadoId;
    }
    
    public String getTipoMarcacion() {
        return tipoMarcacion;
    }
    
    public void setTipoMarcacion(String tipoMarcacion) {
        this.tipoMarcacion = tipoMarcacion;
    }
}