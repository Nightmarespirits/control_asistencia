package com.asistencia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MarcacionRequestDTO {
    
    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos")
    private String dni;
    
    // Constructors
    public MarcacionRequestDTO() {}
    
    public MarcacionRequestDTO(String dni) {
        this.dni = dni;
    }
    
    // Getters and Setters
    public String getDni() {
        return dni;
    }
    
    public void setDni(String dni) {
        this.dni = dni;
    }
    
    @Override
    public String toString() {
        return "MarcacionRequestDTO{" +
                "dni='" + dni + '\'' +
                '}';
    }
}