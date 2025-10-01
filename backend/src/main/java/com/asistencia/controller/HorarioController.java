package com.asistencia.controller;

import com.asistencia.dto.HorarioDTO;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.service.HorarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para la gestión administrativa de horarios
 * Todos los endpoints requieren autenticación JWT
 */
@RestController
@RequestMapping("/api/admin/horarios")
@CrossOrigin(origins = "*")
public class HorarioController {
    
    private static final Logger logger = LoggerFactory.getLogger(HorarioController.class);
    
    @Autowired
    private HorarioService horarioService;
    
    /**
     * Obtiene todos los horarios
     * GET /api/admin/horarios
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllHorarios() {
        try {
            List<HorarioDTO> horarios = horarioService.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", horarios);
            response.put("total", horarios.size());
            
            logger.info("Obtenidos {} horarios", horarios.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al obtener horarios: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al obtener horarios");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtiene todos los horarios activos
     * GET /api/admin/horarios/activos
     */
    @GetMapping("/activos")
    public ResponseEntity<Map<String, Object>> getHorariosActivos() {
        try {
            List<HorarioDTO> horarios = horarioService.findAllActivos();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", horarios);
            response.put("total", horarios.size());
            
            logger.info("Obtenidos {} horarios activos", horarios.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al obtener horarios activos: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al obtener horarios activos");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtiene un horario por ID
     * GET /api/admin/horarios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getHorarioById(@PathVariable Long id) {
        try {
            Optional<HorarioDTO> horario = horarioService.findById(id);
            Map<String, Object> response = new HashMap<>();
            
            if (horario.isPresent()) {
                response.put("success", true);
                response.put("data", horario.get());
                logger.info("Horario encontrado con ID: {}", id);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("mensaje", "Horario no encontrado");
                logger.warn("Horario no encontrado con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error al obtener horario con ID {}: ", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al obtener horario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Crea un nuevo horario
     * POST /api/admin/horarios
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createHorario(@Valid @RequestBody HorarioDTO horarioDTO) {
        try {
            HorarioDTO nuevoHorario = horarioService.create(horarioDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Horario creado exitosamente");
            response.put("data", nuevoHorario);
            
            logger.info("Horario creado exitosamente: {} - {} ({} - {})", 
                nuevoHorario.getNombre(), nuevoHorario.getTipo(), 
                nuevoHorario.getHoraInicio(), nuevoHorario.getHoraFin());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al crear horario: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al crear horario: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error interno al crear horario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Actualiza un horario existente
     * PUT /api/admin/horarios/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateHorario(@PathVariable Long id, @Valid @RequestBody HorarioDTO horarioDTO) {
        try {
            HorarioDTO horarioActualizado = horarioService.update(id, horarioDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Horario actualizado exitosamente");
            response.put("data", horarioActualizado);
            
            logger.info("Horario actualizado exitosamente: {} - {} ({} - {})", 
                horarioActualizado.getNombre(), horarioActualizado.getTipo(), 
                horarioActualizado.getHoraInicio(), horarioActualizado.getHoraFin());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al actualizar horario con ID {}: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al actualizar horario con ID {}: ", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error interno al actualizar horario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Elimina un horario (soft delete)
     * DELETE /api/admin/horarios/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteHorario(@PathVariable Long id) {
        try {
            horarioService.delete(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Horario eliminado exitosamente");
            
            logger.info("Horario eliminado exitosamente con ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al eliminar horario con ID {}: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error al eliminar horario con ID {}: ", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error interno al eliminar horario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Verifica solapamientos de horarios
     * GET /api/admin/horarios/check-overlap
     */
    @GetMapping("/check-overlap")
    public ResponseEntity<Map<String, Object>> checkOverlap(
            @RequestParam TipoMarcacion tipo,
            @RequestParam String horaInicio,
            @RequestParam String horaFin,
            @RequestParam(required = false) Long excludeId) {
        try {
            LocalTime inicio = LocalTime.parse(horaInicio);
            LocalTime fin = LocalTime.parse(horaFin);
            
            boolean hasOverlap = horarioService.existsOverlappingHorario(tipo, inicio, fin, excludeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasOverlap", hasOverlap);
            
            if (hasOverlap) {
                response.put("mensaje", "Existe un solapamiento con otro horario del mismo tipo");
            } else {
                response.put("mensaje", "No hay solapamientos");
            }
            
            logger.info("Verificación de solapamiento: tipo={}, rango={}-{}, overlap={}", 
                tipo, horaInicio, horaFin, hasOverlap);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al verificar solapamiento: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al verificar solapamiento");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Busca horarios por tipo
     * GET /api/admin/horarios/tipo/{tipo}
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<Map<String, Object>> getHorariosByTipo(@PathVariable TipoMarcacion tipo) {
        try {
            Optional<HorarioDTO> horario = horarioService.findByTipo(tipo);
            Map<String, Object> response = new HashMap<>();
            
            if (horario.isPresent()) {
                response.put("success", true);
                response.put("data", horario.get());
                logger.info("Horario encontrado para tipo: {}", tipo);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("mensaje", "No hay horario configurado para el tipo: " + tipo);
                logger.warn("No hay horario configurado para tipo: {}", tipo);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error al buscar horario por tipo {}: ", tipo, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al buscar horario por tipo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Busca horarios por nombre
     * GET /api/admin/horarios/buscar?q={termino}
     */
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> searchHorarios(@RequestParam String q) {
        try {
            List<HorarioDTO> horarios = horarioService.findByNombre(q);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", horarios);
            response.put("total", horarios.size());
            
            logger.info("Encontrados {} horarios con término de búsqueda: {}", horarios.size(), q);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al buscar horarios con término {}: ", q, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al buscar horarios");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtiene horarios que contienen una hora específica
     * GET /api/admin/horarios/en-rango?hora={hora}
     */
    @GetMapping("/en-rango")
    public ResponseEntity<Map<String, Object>> getHorariosEnRango(@RequestParam String hora) {
        try {
            LocalTime horaConsulta = LocalTime.parse(hora);
            List<HorarioDTO> horarios = horarioService.findByHoraEnRango(horaConsulta);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", horarios);
            response.put("total", horarios.size());
            
            logger.info("Encontrados {} horarios que contienen la hora: {}", horarios.size(), hora);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al buscar horarios en rango para hora {}: ", hora, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al buscar horarios en rango");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtiene estadísticas de horarios
     * GET /api/admin/horarios/estadisticas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        try {
            long totalActivos = horarioService.countActivos();
            List<HorarioDTO> todosHorarios = horarioService.findAll();
            long totalInactivos = todosHorarios.size() - totalActivos;
            boolean horariosCompletos = horarioService.tieneHorariosCompletos();
            
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalActivos", totalActivos);
            estadisticas.put("totalInactivos", totalInactivos);
            estadisticas.put("total", todosHorarios.size());
            estadisticas.put("horariosCompletos", horariosCompletos);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", estadisticas);
            
            logger.info("Estadísticas obtenidas: {} activos, {} inactivos, completos: {}", 
                totalActivos, totalInactivos, horariosCompletos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al obtener estadísticas");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Reactiva un horario
     * PUT /api/admin/horarios/{id}/reactivar
     */
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<Map<String, Object>> reactivateHorario(@PathVariable Long id) {
        try {
            Optional<HorarioDTO> horarioOpt = horarioService.findById(id);
            if (horarioOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("mensaje", "Horario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            HorarioDTO horario = horarioOpt.get();
            horario.setActivo(true);
            HorarioDTO horarioReactivado = horarioService.update(id, horario);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Horario reactivado exitosamente");
            response.put("data", horarioReactivado);
            
            logger.info("Horario reactivado exitosamente con ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al reactivar horario con ID {}: ", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error interno al reactivar horario");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}