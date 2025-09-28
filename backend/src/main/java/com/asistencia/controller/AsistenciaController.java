package com.asistencia.controller;

import com.asistencia.dto.MarcacionRequestDTO;
import com.asistencia.dto.MarcacionResponseDTO;
import com.asistencia.exception.EmpleadoNotFoundException;
import com.asistencia.exception.MarcacionDuplicadaException;
import com.asistencia.service.AsistenciaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para el manejo de asistencias
 * Incluye endpoints públicos para marcación directa desde el lector ZKTeco
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AsistenciaController {
    
    private static final Logger logger = LoggerFactory.getLogger(AsistenciaController.class);
    
    @Autowired
    private AsistenciaService asistenciaService;
    
    /**
     * Endpoint público para registrar marcación de asistencia
     * No requiere autenticación para permitir acceso directo del lector ZKTeco
     * 
     * @param request DTO con el DNI del empleado
     * @return Respuesta con el resultado de la marcación
     */
    @PostMapping("/public/asistencia/marcar")
    public ResponseEntity<MarcacionResponseDTO> marcarAsistencia(@Valid @RequestBody MarcacionRequestDTO request) {
        try {
            logger.info("Procesando marcación para DNI: {}", request.getDni());
            
            MarcacionResponseDTO response = asistenciaService.registrarMarcacion(request.getDni());
            
            logger.info("Marcación exitosa para DNI: {} - Tipo: {} - Estado: {}", 
                       request.getDni(), response.getTipo(), response.getEstado());
            
            return ResponseEntity.ok(response);
            
        } catch (EmpleadoNotFoundException e) {
            logger.warn("Empleado no encontrado para DNI: {}", request.getDni());
            MarcacionResponseDTO errorResponse = MarcacionResponseDTO.error("Empleado no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (MarcacionDuplicadaException e) {
            logger.warn("Marcación duplicada para DNI: {} - {}", request.getDni(), e.getMessage());
            MarcacionResponseDTO errorResponse = MarcacionResponseDTO.error("Ya existe una marcación reciente");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            
        } catch (Exception e) {
            logger.error("Error inesperado al procesar marcación para DNI: {}", request.getDni(), e);
            MarcacionResponseDTO errorResponse = MarcacionResponseDTO.error("Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}