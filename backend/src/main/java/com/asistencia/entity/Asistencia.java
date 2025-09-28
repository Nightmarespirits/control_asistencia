package com.asistencia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "asistencias")
public class Asistencia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    @NotNull(message = "El empleado es obligatorio")
    private Empleado empleado;
    
    @Column(name = "fecha_hora", nullable = false)
    @NotNull(message = "La fecha y hora son obligatorias")
    private LocalDateTime fechaHora;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    @NotNull(message = "El tipo de marcación es obligatorio")
    private TipoMarcacion tipo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    private EstadoMarcacion estado = EstadoMarcacion.PUNTUAL;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
    
    // Constructors
    public Asistencia() {}
    
    public Asistencia(Empleado empleado, LocalDateTime fechaHora, TipoMarcacion tipo) {
        this.empleado = empleado;
        this.fechaHora = fechaHora;
        this.tipo = tipo;
    }
    
    public Asistencia(Empleado empleado, LocalDateTime fechaHora, TipoMarcacion tipo, EstadoMarcacion estado) {
        this.empleado = empleado;
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
    
    public Empleado getEmpleado() {
        return empleado;
    }
    
    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
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
        return "Asistencia{" +
                "id=" + id +
                ", empleado=" + (empleado != null ? empleado.getNombreCompleto() : "null") +
                ", fechaHora=" + fechaHora +
                ", tipo=" + tipo +
                ", estado=" + estado +
                ", observaciones='" + observaciones + '\'' +
                '}';
    }
}