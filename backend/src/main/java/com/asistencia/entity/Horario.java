package com.asistencia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "horarios")
public class Horario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre", nullable = false, length = 50)
    @NotBlank(message = "El nombre del horario es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;
    
    @Column(name = "hora_inicio", nullable = false)
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;
    
    @Column(name = "hora_fin", nullable = false)
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime horaFin;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    @NotNull(message = "El tipo de horario es obligatorio")
    private TipoMarcacion tipo;
    
    @Column(name = "activo")
    private Boolean activo = true;
    
    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
    
    // Constructors
    public Horario() {}
    
    public Horario(String nombre, LocalTime horaInicio, LocalTime horaFin, TipoMarcacion tipo) {
        this.nombre = nombre;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.tipo = tipo;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public LocalTime getHoraInicio() {
        return horaInicio;
    }
    
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }
    
    public LocalTime getHoraFin() {
        return horaFin;
    }
    
    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }
    
    public TipoMarcacion getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoMarcacion tipo) {
        this.tipo = tipo;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    // Helper methods
    public boolean estaEnRango(LocalTime hora) {
        return !hora.isBefore(horaInicio) && !hora.isAfter(horaFin);
    }
    
    public String getRangoHorario() {
        return horaInicio + " - " + horaFin;
    }
    
    @Override
    public String toString() {
        return "Horario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", horaInicio=" + horaInicio +
                ", horaFin=" + horaFin +
                ", tipo=" + tipo +
                ", activo=" + activo +
                '}';
    }
}