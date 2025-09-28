package com.asistencia.controller;

import com.asistencia.dto.EmpleadoDTO;
import com.asistencia.service.EmpleadoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para la gestión administrativa de empleados
 * Todos los endpoints requieren autenticación JWT
 */
@RestController
@RequestMapping("/api/admin/empleados")
@CrossOrigin(origins = "*")
public class EmpleadoController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmpleadoController.class);
    
    @Autowired
    private EmpleadoService empleadoService;
    
    /**
     * Obtiene todos los empleados activos
     * GET /api/admin/empleados
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEmpleados() {
        try {
            List<EmpleadoDTO> empleados = empleadoService.findAllActivos();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", empleados);
            response.put("total", empleados.size());
            
            logger.info("Obtenidos {} empleados activos", empleados.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al obtener empleados: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al obtener empleados");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtiene todos los empleados (incluyendo inactivos)
     * GET /api/admin/empleados/all
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllEmpleadosIncludingInactive() {
        try {
            List<EmpleadoDTO> empleados = empleadoService.findAll();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", empleados);
            response.put("total", empleados.size());
            
            logger.info("Obtenidos {} empleados (incluyendo inactivos)", empleados.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al obtener todos los empleados: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al obtener empleados");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtiene un empleado por ID
     * GET /api/admin/empleados/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEmpleadoById(@PathVariable Long id) {
        try {
            Optional<EmpleadoDTO> empleado = empleadoService.findById(id);
            Map<String, Object> response = new HashMap<>();
            
            if (empleado.isPresent()) {
                response.put("success", true);
                response.put("data", empleado.get());
                logger.info("Empleado encontrado con ID: {}", id);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("mensaje", "Empleado no encontrado");
                logger.warn("Empleado no encontrado con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error al obtener empleado con ID {}: ", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al obtener empleado");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Busca empleados por DNI
     * GET /api/admin/empleados/buscar/dni/{dni}
     */
    @GetMapping("/buscar/dni/{dni}")
    public ResponseEntity<Map<String, Object>> getEmpleadoByDni(@PathVariable String dni) {
        try {
            Optional<EmpleadoDTO> empleado = empleadoService.findByDni(dni);
            Map<String, Object> response = new HashMap<>();
            
            if (empleado.isPresent()) {
                response.put("success", true);
                response.put("data", empleado.get());
                logger.info("Empleado encontrado con DNI: {}", dni);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("mensaje", "Empleado no encontrado con DNI: " + dni);
                logger.warn("Empleado no encontrado con DNI: {}", dni);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error al buscar empleado con DNI {}: ", dni, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al buscar empleado");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Busca empleados por área
     * GET /api/admin/empleados/buscar/area/{area}
     */
    @GetMapping("/buscar/area/{area}")
    public ResponseEntity<Map<String, Object>> getEmpleadosByArea(@PathVariable String area) {
        try {
            List<EmpleadoDTO> empleados = empleadoService.findByArea(area);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", empleados);
            response.put("total", empleados.size());
            
            logger.info("Encontrados {} empleados en el área: {}", empleados.size(), area);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al buscar empleados por área {}: ", area, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al buscar empleados por área");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Busca empleados por cargo
     * GET /api/admin/empleados/buscar/cargo/{cargo}
     */
    @GetMapping("/buscar/cargo/{cargo}")
    public ResponseEntity<Map<String, Object>> getEmpleadosByCargo(@PathVariable String cargo) {
        try {
            List<EmpleadoDTO> empleados = empleadoService.findByCargo(cargo);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", empleados);
            response.put("total", empleados.size());
            
            logger.info("Encontrados {} empleados con cargo: {}", empleados.size(), cargo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al buscar empleados por cargo {}: ", cargo, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al buscar empleados por cargo");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Busca empleados por nombre o apellido
     * GET /api/admin/empleados/buscar?q={termino}
     */
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> searchEmpleados(@RequestParam String q) {
        try {
            List<EmpleadoDTO> empleados = empleadoService.findByNombreOrApellido(q);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", empleados);
            response.put("total", empleados.size());
            
            logger.info("Encontrados {} empleados con término de búsqueda: {}", empleados.size(), q);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al buscar empleados con término {}: ", q, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al buscar empleados");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Crea un nuevo empleado
     * POST /api/admin/empleados
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createEmpleado(@Valid @RequestBody EmpleadoDTO empleadoDTO) {
        try {
            EmpleadoDTO nuevoEmpleado = empleadoService.create(empleadoDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Empleado creado exitosamente");
            response.put("data", nuevoEmpleado);
            
            logger.info("Empleado creado exitosamente: {} - {}", nuevoEmpleado.getDni(), nuevoEmpleado.getNombreCompleto());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al crear empleado: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al crear empleado: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error interno al crear empleado");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Actualiza un empleado existente
     * PUT /api/admin/empleados/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateEmpleado(@PathVariable Long id, @Valid @RequestBody EmpleadoDTO empleadoDTO) {
        try {
            EmpleadoDTO empleadoActualizado = empleadoService.update(id, empleadoDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Empleado actualizado exitosamente");
            response.put("data", empleadoActualizado);
            
            logger.info("Empleado actualizado exitosamente: {} - {}", empleadoActualizado.getDni(), empleadoActualizado.getNombreCompleto());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al actualizar empleado con ID {}: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error al actualizar empleado con ID {}: ", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error interno al actualizar empleado");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Elimina un empleado (soft delete)
     * DELETE /api/admin/empleados/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteEmpleado(@PathVariable Long id) {
        try {
            empleadoService.delete(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Empleado desactivado exitosamente");
            
            logger.info("Empleado desactivado exitosamente con ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Error al desactivar empleado con ID {}: {}", id, e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error al desactivar empleado con ID {}: ", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error interno al desactivar empleado");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Reactiva un empleado
     * PUT /api/admin/empleados/{id}/reactivar
     */
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<Map<String, Object>> reactivateEmpleado(@PathVariable Long id) {
        try {
            Optional<EmpleadoDTO> empleadoOpt = empleadoService.findById(id);
            if (empleadoOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("mensaje", "Empleado no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            EmpleadoDTO empleado = empleadoOpt.get();
            empleado.setActivo(true);
            EmpleadoDTO empleadoReactivado = empleadoService.update(id, empleado);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("mensaje", "Empleado reactivado exitosamente");
            response.put("data", empleadoReactivado);
            
            logger.info("Empleado reactivado exitosamente con ID: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al reactivar empleado con ID {}: ", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error interno al reactivar empleado");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Obtiene estadísticas de empleados
     * GET /api/admin/empleados/estadisticas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        try {
            long totalActivos = empleadoService.countActivos();
            List<EmpleadoDTO> todosEmpleados = empleadoService.findAll();
            long totalInactivos = todosEmpleados.size() - totalActivos;
            
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalActivos", totalActivos);
            estadisticas.put("totalInactivos", totalInactivos);
            estadisticas.put("total", todosEmpleados.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", estadisticas);
            
            logger.info("Estadísticas obtenidas: {} activos, {} inactivos", totalActivos, totalInactivos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("mensaje", "Error al obtener estadísticas");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}