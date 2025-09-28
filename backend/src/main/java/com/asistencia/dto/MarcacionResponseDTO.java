package com.asistencia.dto;

import com.asistencia.entity.EstadoMarcacion;
import com.asistencia.entity.TipoMarcacion;

import java.time.LocalDateTime;

public class MarcacionResponseDTO {
    
    private boolean success;
    private String mensaje;
    private EmpleadoDTO empleado;
    private TipoMarcacion tipo;
    private EstadoMarcacion estado;
    private LocalDateTime fechaHora;
    private String observaciones;
    
    // Constructors
    public MarcacionResponseDTO() {}
    
    public MarcacionResponseDTO(boolean success, String mensaje) {
        this.success = success;
        this.mensaje = mensaje;
    }
    
    public MarcacionResponseDTO(boolean success, String mensaje, EmpleadoDTO empleado, 
                               TipoMarcacion tipo, EstadoMarcacion estado, LocalDateTime fechaHora) {
        this.success = success;
        this.mensaje = mensaje;
        this.empleado = empleado;
        this.tipo = tipo;
        this.estado = estado;
        this.fechaHora = fechaHora;
    }
    
    // Static factory methods for common responses
    public static MarcacionResponseDTO success(String mensaje, EmpleadoDTO empleado, 
                                              TipoMarcacion tipo, EstadoMarcacion estado, 
                                              LocalDateTime fechaHora) {
        return new MarcacionResponseDTO(true, mensaje, empleado, tipo, estado, fechaHora);
    }
    
    public static MarcacionResponseDTO error(String mensaje) {
        return new MarcacionResponseDTO(false, mensaje);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    public EmpleadoDTO getEmpleado() {
        return empleado;
    }
    
    public void setEmpleado(EmpleadoDTO empleado) {
        this.empleado = empleado;
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
    
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    @Override
    public String toString() {
        return "MarcacionResponseDTO{" +
                "success=" + success +
                ", mensaje='" + mensaje + '\'' +
                ", empleado=" + empleado +
                ", tipo=" + tipo +
                ", estado=" + estado +
                ", fechaHora=" + fechaHora +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}