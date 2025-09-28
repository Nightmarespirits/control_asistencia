package com.asistencia.dto;

import com.asistencia.entity.EstadoMarcacion;
import com.asistencia.entity.TipoMarcacion;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class ReporteAsistenciaDTO {
    
    private Long id;
    private String empleadoNombres;
    private String empleadoApellidos;
    private String empleadoDni;
    private String empleadoCargo;
    private String empleadoArea;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaHora;
    
    private TipoMarcacion tipo;
    private EstadoMarcacion estado;
    private String observaciones;
    
    // Constructors
    public ReporteAsistenciaDTO() {}
    
    public ReporteAsistenciaDTO(Long id, String empleadoNombres, String empleadoApellidos, 
                               String empleadoDni, String empleadoCargo, String empleadoArea,
                               LocalDateTime fechaHora, TipoMarcacion tipo, 
                               EstadoMarcacion estado, String observaciones) {
        this.id = id;
        this.empleadoNombres = empleadoNombres;
        this.empleadoApellidos = empleadoApellidos;
        this.empleadoDni = empleadoDni;
        this.empleadoCargo = empleadoCargo;
        this.empleadoArea = empleadoArea;
        this.fechaHora = fechaHora;
        this.tipo = tipo;
        this.estado = estado;
        this.observaciones = observaciones;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmpleadoNombres() {
        return empleadoNombres;
    }
    
    public void setEmpleadoNombres(String empleadoNombres) {
        this.empleadoNombres = empleadoNombres;
    }
    
    public String getEmpleadoApellidos() {
        return empleadoApellidos;
    }
    
    public void setEmpleadoApellidos(String empleadoApellidos) {
        this.empleadoApellidos = empleadoApellidos;
    }
    
    public String getEmpleadoDni() {
        return empleadoDni;
    }
    
    public void setEmpleadoDni(String empleadoDni) {
        this.empleadoDni = empleadoDni;
    }
    
    public String getEmpleadoCargo() {
        return empleadoCargo;
    }
    
    public void setEmpleadoCargo(String empleadoCargo) {
        this.empleadoCargo = empleadoCargo;
    }
    
    public String getEmpleadoArea() {
        return empleadoArea;
    }
    
    public void setEmpleadoArea(String empleadoArea) {
        this.empleadoArea = empleadoArea;
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
    
    public String getNombreCompleto() {
        return empleadoNombres + " " + empleadoApellidos;
    }
}