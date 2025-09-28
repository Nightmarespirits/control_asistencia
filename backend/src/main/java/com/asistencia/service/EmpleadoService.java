package com.asistencia.service;

import com.asistencia.dto.EmpleadoDTO;
import com.asistencia.entity.Empleado;
import com.asistencia.exception.EmpleadoNotFoundException;
import com.asistencia.exception.EmpleadoValidationException;
import com.asistencia.repository.EmpleadoRepository;
import com.asistencia.util.CodigoUnicoGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmpleadoService {
    
    @Autowired
    private EmpleadoRepository empleadoRepository;
    
    // CodigoUnicoGenerator uses static methods, no need for injection
    
    /**
     * Obtiene todos los empleados activos
     */
    @Transactional(readOnly = true)
    public List<EmpleadoDTO> findAllActivos() {
        return empleadoRepository.findByActivoTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los empleados
     */
    @Transactional(readOnly = true)
    public List<EmpleadoDTO> findAll() {
        return empleadoRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca un empleado por ID
     */
    @Transactional(readOnly = true)
    public Optional<EmpleadoDTO> findById(Long id) {
        return empleadoRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Busca un empleado por DNI
     */
    @Transactional(readOnly = true)
    public Optional<EmpleadoDTO> findByDni(String dni) {
        return empleadoRepository.findByDni(dni)
                .map(this::convertToDTO);
    }
    
    /**
     * Busca un empleado por código único
     */
    @Transactional(readOnly = true)
    public Optional<EmpleadoDTO> findByCodigoUnico(String codigoUnico) {
        return empleadoRepository.findByCodigoUnico(codigoUnico)
                .map(this::convertToDTO);
    }
    
    /**
     * Busca empleados por área
     */
    @Transactional(readOnly = true)
    public List<EmpleadoDTO> findByArea(String area) {
        return empleadoRepository.findByAreaAndActivoTrue(area)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca empleados por cargo
     */
    @Transactional(readOnly = true)
    public List<EmpleadoDTO> findByCargo(String cargo) {
        return empleadoRepository.findByCargoAndActivoTrue(cargo)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca empleados por nombre o apellido
     */
    @Transactional(readOnly = true)
    public List<EmpleadoDTO> findByNombreOrApellido(String termino) {
        return empleadoRepository.findByNombreOrApellidoContainingIgnoreCase(termino)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Crea un nuevo empleado
     */
    public EmpleadoDTO create(EmpleadoDTO empleadoDTO) {
        // Validaciones de negocio
        validateEmpleadoData(empleadoDTO);
        
        // Validar que el DNI no exista
        if (empleadoRepository.existsByDni(empleadoDTO.getDni())) {
            throw new EmpleadoValidationException("Ya existe un empleado con el DNI: " + empleadoDTO.getDni());
        }
        
        Empleado empleado = convertToEntity(empleadoDTO);
        
        // Generar código único automáticamente
        String codigoUnico = generateUniqueCode();
        empleado.setCodigoUnico(codigoUnico);
        empleado.setActivo(true);
        
        Empleado savedEmpleado = empleadoRepository.save(empleado);
        return convertToDTO(savedEmpleado);
    }
    
    /**
     * Actualiza un empleado existente
     */
    public EmpleadoDTO update(Long id, EmpleadoDTO empleadoDTO) {
        Empleado empleadoExistente = empleadoRepository.findById(id)
                .orElseThrow(() -> new EmpleadoNotFoundException("Empleado no encontrado con ID: " + id));
        
        // Validaciones de negocio
        validateEmpleadoData(empleadoDTO);
        
        // Validar que el DNI no esté en uso por otro empleado
        Optional<Empleado> empleadoConMismoDni = empleadoRepository.findByDni(empleadoDTO.getDni());
        if (empleadoConMismoDni.isPresent() && !empleadoConMismoDni.get().getId().equals(id)) {
            throw new EmpleadoValidationException("Ya existe otro empleado con el DNI: " + empleadoDTO.getDni());
        }
        
        // Actualizar campos (no se permite cambiar el código único)
        empleadoExistente.setDni(empleadoDTO.getDni());
        empleadoExistente.setNombres(empleadoDTO.getNombres());
        empleadoExistente.setApellidos(empleadoDTO.getApellidos());
        empleadoExistente.setCargo(empleadoDTO.getCargo());
        empleadoExistente.setArea(empleadoDTO.getArea());
        
        if (empleadoDTO.getActivo() != null) {
            empleadoExistente.setActivo(empleadoDTO.getActivo());
        }
        
        Empleado updatedEmpleado = empleadoRepository.save(empleadoExistente);
        return convertToDTO(updatedEmpleado);
    }
    
    /**
     * Elimina un empleado (soft delete)
     * Mantiene los registros históricos de asistencia
     */
    public void delete(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new EmpleadoNotFoundException("Empleado no encontrado con ID: " + id));
        
        // Soft delete - solo marca como inactivo para mantener registros históricos
        empleado.setActivo(false);
        empleadoRepository.save(empleado);
    }
    
    /**
     * Elimina permanentemente un empleado
     */
    public void deletePhysically(Long id) {
        if (!empleadoRepository.existsById(id)) {
            throw new IllegalArgumentException("Empleado no encontrado con ID: " + id);
        }
        empleadoRepository.deleteById(id);
    }
    
    /**
     * Verifica si existe un empleado con el DNI especificado
     */
    @Transactional(readOnly = true)
    public boolean existsByDni(String dni) {
        return empleadoRepository.existsByDni(dni);
    }
    
    /**
     * Cuenta empleados activos
     */
    @Transactional(readOnly = true)
    public long countActivos() {
        return empleadoRepository.countByActivoTrue();
    }
    
    /**
     * Cuenta empleados por área
     */
    @Transactional(readOnly = true)
    public long countByArea(String area) {
        return empleadoRepository.countByAreaAndActivoTrue(area);
    }
    
    // Métodos de conversión
    private EmpleadoDTO convertToDTO(Empleado empleado) {
        EmpleadoDTO dto = new EmpleadoDTO();
        dto.setId(empleado.getId());
        dto.setCodigoUnico(empleado.getCodigoUnico());
        dto.setDni(empleado.getDni());
        dto.setNombres(empleado.getNombres());
        dto.setApellidos(empleado.getApellidos());
        dto.setCargo(empleado.getCargo());
        dto.setArea(empleado.getArea());
        dto.setActivo(empleado.getActivo());
        dto.setFechaCreacion(empleado.getFechaCreacion());
        dto.setFechaActualizacion(empleado.getFechaActualizacion());
        return dto;
    }
    
    private Empleado convertToEntity(EmpleadoDTO dto) {
        Empleado empleado = new Empleado();
        empleado.setId(dto.getId());
        empleado.setCodigoUnico(dto.getCodigoUnico());
        empleado.setDni(dto.getDni());
        empleado.setNombres(dto.getNombres());
        empleado.setApellidos(dto.getApellidos());
        empleado.setCargo(dto.getCargo());
        empleado.setArea(dto.getArea());
        empleado.setActivo(dto.getActivo());
        return empleado;
    }
    
    /**
     * Obtiene la entidad Empleado por ID (para uso interno)
     */
    @Transactional(readOnly = true)
    public Optional<Empleado> findEntityById(Long id) {
        return empleadoRepository.findById(id);
    }
    
    /**
     * Obtiene la entidad Empleado por DNI (para uso interno)
     */
    @Transactional(readOnly = true)
    public Optional<Empleado> findEntityByDni(String dni) {
        return empleadoRepository.findByDni(dni);
    }
    
    /**
     * Valida los datos del empleado
     */
    private void validateEmpleadoData(EmpleadoDTO empleadoDTO) {
        if (empleadoDTO == null) {
            throw new EmpleadoValidationException("Los datos del empleado son obligatorios");
        }
        
        if (empleadoDTO.getDni() == null || empleadoDTO.getDni().trim().isEmpty()) {
            throw new EmpleadoValidationException("El DNI es obligatorio");
        }
        
        if (!empleadoDTO.getDni().matches("\\d{8}")) {
            throw new EmpleadoValidationException("El DNI debe tener exactamente 8 dígitos");
        }
        
        if (empleadoDTO.getNombres() == null || empleadoDTO.getNombres().trim().isEmpty()) {
            throw new EmpleadoValidationException("Los nombres son obligatorios");
        }
        
        if (empleadoDTO.getApellidos() == null || empleadoDTO.getApellidos().trim().isEmpty()) {
            throw new EmpleadoValidationException("Los apellidos son obligatorios");
        }
        
        if (empleadoDTO.getCargo() == null || empleadoDTO.getCargo().trim().isEmpty()) {
            throw new EmpleadoValidationException("El cargo es obligatorio");
        }
        
        if (empleadoDTO.getArea() == null || empleadoDTO.getArea().trim().isEmpty()) {
            throw new EmpleadoValidationException("El área es obligatoria");
        }
        
        // Validar longitudes máximas
        if (empleadoDTO.getNombres().length() > 100) {
            throw new EmpleadoValidationException("Los nombres no pueden exceder 100 caracteres");
        }
        
        if (empleadoDTO.getApellidos().length() > 100) {
            throw new EmpleadoValidationException("Los apellidos no pueden exceder 100 caracteres");
        }
        
        if (empleadoDTO.getCargo().length() > 100) {
            throw new EmpleadoValidationException("El cargo no puede exceder 100 caracteres");
        }
        
        if (empleadoDTO.getArea().length() > 100) {
            throw new EmpleadoValidationException("El área no puede exceder 100 caracteres");
        }
    }
    
    /**
     * Genera un código único para el empleado
     */
    private String generateUniqueCode() {
        String codigoUnico;
        int intentos = 0;
        int maxIntentos = 10;
        
        do {
            codigoUnico = CodigoUnicoGenerator.generarCodigoEmpleado();
            intentos++;
            
            if (intentos > maxIntentos) {
                throw new EmpleadoValidationException("No se pudo generar un código único después de " + maxIntentos + " intentos");
            }
        } while (empleadoRepository.existsByCodigoUnico(codigoUnico));
        
        return codigoUnico;
    }
}