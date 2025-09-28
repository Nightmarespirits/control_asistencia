package com.asistencia.service;

import com.asistencia.dto.AsistenciaDTO;
import com.asistencia.dto.EmpleadoDTO;
import com.asistencia.dto.MarcacionResponseDTO;
import com.asistencia.entity.Asistencia;
import com.asistencia.entity.Empleado;
import com.asistencia.entity.EstadoMarcacion;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.exception.EmpleadoNotFoundException;
import com.asistencia.exception.MarcacionDuplicadaException;
import com.asistencia.repository.AsistenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AsistenciaService {
    
    @Autowired
    private AsistenciaRepository asistenciaRepository;
    
    @Autowired
    private EmpleadoService empleadoService;
    
    @Autowired
    private HorarioService horarioService;
    
    /**
     * Obtiene todas las asistencias
     */
    @Transactional(readOnly = true)
    public List<AsistenciaDTO> findAll() {
        return asistenciaRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca una asistencia por ID
     */
    @Transactional(readOnly = true)
    public Optional<AsistenciaDTO> findById(Long id) {
        return asistenciaRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Busca asistencias por empleado
     */
    @Transactional(readOnly = true)
    public List<AsistenciaDTO> findByEmpleadoId(Long empleadoId) {
        Optional<Empleado> empleado = empleadoService.findEntityById(empleadoId);
        if (empleado.isPresent()) {
            return asistenciaRepository.findByEmpleadoOrderByFechaHoraDesc(empleado.get())
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
    
    /**
     * Busca asistencias por empleado en un rango de fechas
     */
    @Transactional(readOnly = true)
    public List<AsistenciaDTO> findByEmpleadoAndFechaRange(Long empleadoId, 
                                                          LocalDateTime fechaInicio, 
                                                          LocalDateTime fechaFin) {
        Optional<Empleado> empleado = empleadoService.findEntityById(empleadoId);
        if (empleado.isPresent()) {
            return asistenciaRepository.findByEmpleadoAndFechaHoraBetween(empleado.get(), fechaInicio, fechaFin)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
    
    /**
     * Busca asistencias en un rango de fechas
     */
    @Transactional(readOnly = true)
    public List<AsistenciaDTO> findByFechaRange(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return asistenciaRepository.findByFechaHoraBetween(fechaInicio, fechaFin)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca asistencias por tipo de marcación
     */
    @Transactional(readOnly = true)
    public List<AsistenciaDTO> findByTipo(TipoMarcacion tipo) {
        return asistenciaRepository.findByTipoOrderByFechaHoraDesc(tipo)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca asistencias con filtros múltiples para reportes
     */
    @Transactional(readOnly = true)
    public List<AsistenciaDTO> findWithFilters(Long empleadoId, TipoMarcacion tipo, 
                                              LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        Empleado empleado = null;
        if (empleadoId != null) {
            empleado = empleadoService.findEntityById(empleadoId).orElse(null);
        }
        
        return asistenciaRepository.findWithFilters(empleado, tipo, fechaInicio, fechaFin)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene la última marcación de un empleado
     */
    @Transactional(readOnly = true)
    public Optional<AsistenciaDTO> findLastMarcacionByEmpleado(Long empleadoId) {
        Optional<Empleado> empleado = empleadoService.findEntityById(empleadoId);
        if (empleado.isPresent()) {
            return asistenciaRepository.findFirstByEmpleadoOrderByFechaHoraDesc(empleado.get())
                    .map(this::convertToDTO);
        }
        return Optional.empty();
    }
    
    /**
     * Obtiene las asistencias del día actual por empleado
     */
    @Transactional(readOnly = true)
    public List<AsistenciaDTO> findByEmpleadoToday(Long empleadoId) {
        Optional<Empleado> empleado = empleadoService.findEntityById(empleadoId);
        if (empleado.isPresent()) {
            LocalDateTime inicioDelDia = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime finDelDia = inicioDelDia.plusDays(1);
            
            return asistenciaRepository.findByEmpleadoAndToday(empleado.get(), inicioDelDia, finDelDia)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
    
    /**
     * Crea una nueva asistencia
     */
    public AsistenciaDTO create(AsistenciaDTO asistenciaDTO) {
        Empleado empleado = empleadoService.findEntityById(asistenciaDTO.getEmpleadoId())
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con ID: " + asistenciaDTO.getEmpleadoId()));
        
        Asistencia asistencia = convertToEntity(asistenciaDTO);
        asistencia.setEmpleado(empleado);
        
        // Establecer fecha y hora actual si no se proporciona
        if (asistencia.getFechaHora() == null) {
            asistencia.setFechaHora(LocalDateTime.now());
        }
        
        // Establecer estado por defecto si no se proporciona
        if (asistencia.getEstado() == null) {
            asistencia.setEstado(EstadoMarcacion.PUNTUAL);
        }
        
        Asistencia savedAsistencia = asistenciaRepository.save(asistencia);
        return convertToDTO(savedAsistencia);
    }
    
    /**
     * Actualiza una asistencia existente
     */
    public AsistenciaDTO update(Long id, AsistenciaDTO asistenciaDTO) {
        Asistencia asistenciaExistente = asistenciaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asistencia no encontrada con ID: " + id));
        
        // Actualizar campos
        if (asistenciaDTO.getFechaHora() != null) {
            asistenciaExistente.setFechaHora(asistenciaDTO.getFechaHora());
        }
        
        if (asistenciaDTO.getTipo() != null) {
            asistenciaExistente.setTipo(asistenciaDTO.getTipo());
        }
        
        if (asistenciaDTO.getEstado() != null) {
            asistenciaExistente.setEstado(asistenciaDTO.getEstado());
        }
        
        if (asistenciaDTO.getObservaciones() != null) {
            asistenciaExistente.setObservaciones(asistenciaDTO.getObservaciones());
        }
        
        Asistencia updatedAsistencia = asistenciaRepository.save(asistenciaExistente);
        return convertToDTO(updatedAsistencia);
    }
    
    /**
     * Elimina una asistencia
     */
    public void delete(Long id) {
        if (!asistenciaRepository.existsById(id)) {
            throw new IllegalArgumentException("Asistencia no encontrada con ID: " + id);
        }
        asistenciaRepository.deleteById(id);
    }
    
    /**
     * Verifica si existe una marcación reciente del mismo tipo
     */
    @Transactional(readOnly = true)
    public boolean existsRecentMarcacion(Long empleadoId, TipoMarcacion tipo, int minutosRango) {
        Optional<Empleado> empleado = empleadoService.findEntityById(empleadoId);
        if (empleado.isPresent()) {
            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime fechaInicio = ahora.minusMinutes(minutosRango);
            LocalDateTime fechaFin = ahora.plusMinutes(minutosRango);
            
            return asistenciaRepository.existsRecentMarcacion(empleado.get(), tipo, fechaInicio, fechaFin);
        }
        return false;
    }
    
    /**
     * Cuenta asistencias por empleado en un rango de fechas
     */
    @Transactional(readOnly = true)
    public long countByEmpleadoAndFechaRange(Long empleadoId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        Optional<Empleado> empleado = empleadoService.findEntityById(empleadoId);
        if (empleado.isPresent()) {
            return asistenciaRepository.countByEmpleadoAndFechaHoraBetween(empleado.get(), fechaInicio, fechaFin);
        }
        return 0;
    }
    
    /**
     * Registra una marcación automática basada en el DNI del empleado
     * Determina automáticamente el tipo de marcación y estado según la hora actual
     */
    public MarcacionResponseDTO registrarMarcacion(String dni) {
        // Buscar empleado por DNI
        Optional<Empleado> empleadoOpt = empleadoService.findEntityByDni(dni);
        if (empleadoOpt.isEmpty()) {
            throw EmpleadoNotFoundException.porDni(dni);
        }
        
        Empleado empleado = empleadoOpt.get();
        LocalDateTime ahora = LocalDateTime.now();
        
        // Determinar tipo de marcación basado en la hora actual
        TipoMarcacion tipoMarcacion = horarioService.determinarTipoMarcacion(ahora.toLocalTime());
        
        // Verificar marcación duplicada (5 minutos de rango)
        if (existsRecentMarcacion(empleado.getId(), tipoMarcacion, 5)) {
            throw new MarcacionDuplicadaException(dni, tipoMarcacion.getDescripcion());
        }
        
        // Calcular estado de la marcación
        EstadoMarcacion estadoMarcacion = horarioService.calcularEstadoMarcacion(ahora.toLocalTime(), tipoMarcacion);
        
        // Generar mensaje contextual
        String mensaje = horarioService.generarMensajeMarcacion(ahora.toLocalTime(), tipoMarcacion, estadoMarcacion);
        
        // Crear y guardar la asistencia
        Asistencia asistencia = new Asistencia();
        asistencia.setEmpleado(empleado);
        asistencia.setFechaHora(ahora);
        asistencia.setTipo(tipoMarcacion);
        asistencia.setEstado(estadoMarcacion);
        
        // Agregar observaciones si es necesario
        if (estadoMarcacion == EstadoMarcacion.TARDANZA) {
            int minutosTarde = horarioService.calcularMinutosDiferencia(ahora.toLocalTime(), tipoMarcacion);
            if (minutosTarde > 0) {
                asistencia.setObservaciones("Tardanza de " + minutosTarde + " minutos");
            }
        } else if (estadoMarcacion == EstadoMarcacion.FUERA_HORARIO) {
            asistencia.setObservaciones("Marcación fuera de horario laboral");
        }
        
        asistenciaRepository.save(asistencia);
        
        // Crear DTO del empleado para la respuesta
        EmpleadoDTO empleadoDTO = new EmpleadoDTO();
        empleadoDTO.setId(empleado.getId());
        empleadoDTO.setNombres(empleado.getNombres());
        empleadoDTO.setApellidos(empleado.getApellidos());
        empleadoDTO.setDni(empleado.getDni());
        
        // Crear respuesta exitosa
        MarcacionResponseDTO response = MarcacionResponseDTO.success(mensaje, empleadoDTO, tipoMarcacion, estadoMarcacion, ahora);
        response.setObservaciones(asistencia.getObservaciones());
        
        return response;
    }
    

    
    // Métodos de conversión
    private AsistenciaDTO convertToDTO(Asistencia asistencia) {
        AsistenciaDTO dto = new AsistenciaDTO();
        dto.setId(asistencia.getId());
        dto.setEmpleadoId(asistencia.getEmpleado().getId());
        dto.setEmpleadoNombre(asistencia.getEmpleado().getNombreCompleto());
        dto.setEmpleadoDni(asistencia.getEmpleado().getDni());
        dto.setFechaHora(asistencia.getFechaHora());
        dto.setTipo(asistencia.getTipo());
        dto.setEstado(asistencia.getEstado());
        dto.setObservaciones(asistencia.getObservaciones());
        dto.setFechaCreacion(asistencia.getFechaCreacion());
        return dto;
    }
    
    private Asistencia convertToEntity(AsistenciaDTO dto) {
        Asistencia asistencia = new Asistencia();
        asistencia.setId(dto.getId());
        asistencia.setFechaHora(dto.getFechaHora());
        asistencia.setTipo(dto.getTipo());
        asistencia.setEstado(dto.getEstado());
        asistencia.setObservaciones(dto.getObservaciones());
        return asistencia;
    }
}