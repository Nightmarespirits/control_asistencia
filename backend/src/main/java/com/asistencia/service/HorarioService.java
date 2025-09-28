package com.asistencia.service;

import com.asistencia.dto.HorarioDTO;
import com.asistencia.entity.EstadoMarcacion;
import com.asistencia.entity.Horario;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.repository.HorarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class HorarioService {
    
    @Autowired
    private HorarioRepository horarioRepository;
    
    /**
     * Obtiene todos los horarios
     */
    @Transactional(readOnly = true)
    public List<HorarioDTO> findAll() {
        return horarioRepository.findAllByOrderByHoraInicio()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los horarios activos
     */
    @Transactional(readOnly = true)
    public List<HorarioDTO> findAllActivos() {
        return horarioRepository.findByActivoTrueOrderByHoraInicio()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca un horario por ID
     */
    @Transactional(readOnly = true)
    public Optional<HorarioDTO> findById(Long id) {
        return horarioRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    /**
     * Busca horario por tipo de marcación
     */
    @Transactional(readOnly = true)
    public Optional<HorarioDTO> findByTipo(TipoMarcacion tipo) {
        return horarioRepository.findByTipoAndActivoTrue(tipo)
                .map(this::convertToDTO);
    }
    
    /**
     * Busca horarios que contengan una hora específica
     */
    @Transactional(readOnly = true)
    public List<HorarioDTO> findByHoraEnRango(LocalTime hora) {
        return horarioRepository.findByHoraEnRango(hora)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca el horario más apropiado para una hora específica
     */
    @Transactional(readOnly = true)
    public Optional<HorarioDTO> findBestMatchForTime(LocalTime hora) {
        return horarioRepository.findBestMatchForTime(hora)
                .map(this::convertToDTO);
    }
    
    /**
     * Busca horarios por nombre
     */
    @Transactional(readOnly = true)
    public List<HorarioDTO> findByNombre(String nombre) {
        return horarioRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Crea un nuevo horario
     */
    public HorarioDTO create(HorarioDTO horarioDTO) {
        // Validar que no haya solapamientos
        if (existsOverlappingHorario(horarioDTO.getTipo(), horarioDTO.getHoraInicio(), 
                                   horarioDTO.getHoraFin(), null)) {
            throw new IllegalArgumentException("Ya existe un horario que se solapa con el rango especificado para el tipo: " + horarioDTO.getTipo());
        }
        
        // Validar que la hora de inicio sea menor que la hora de fin
        if (horarioDTO.getHoraInicio().isAfter(horarioDTO.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser menor que la hora de fin");
        }
        
        Horario horario = convertToEntity(horarioDTO);
        horario.setActivo(true);
        
        Horario savedHorario = horarioRepository.save(horario);
        return convertToDTO(savedHorario);
    }
    
    /**
     * Actualiza un horario existente
     */
    public HorarioDTO update(Long id, HorarioDTO horarioDTO) {
        Horario horarioExistente = horarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con ID: " + id));
        
        // Validar que no haya solapamientos (excluyendo el horario actual)
        if (existsOverlappingHorario(horarioDTO.getTipo(), horarioDTO.getHoraInicio(), 
                                   horarioDTO.getHoraFin(), id)) {
            throw new IllegalArgumentException("Ya existe un horario que se solapa con el rango especificado para el tipo: " + horarioDTO.getTipo());
        }
        
        // Validar que la hora de inicio sea menor que la hora de fin
        if (horarioDTO.getHoraInicio().isAfter(horarioDTO.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser menor que la hora de fin");
        }
        
        // Actualizar campos
        horarioExistente.setNombre(horarioDTO.getNombre());
        horarioExistente.setHoraInicio(horarioDTO.getHoraInicio());
        horarioExistente.setHoraFin(horarioDTO.getHoraFin());
        horarioExistente.setTipo(horarioDTO.getTipo());
        
        if (horarioDTO.getActivo() != null) {
            horarioExistente.setActivo(horarioDTO.getActivo());
        }
        
        Horario updatedHorario = horarioRepository.save(horarioExistente);
        return convertToDTO(updatedHorario);
    }
    
    /**
     * Elimina un horario (soft delete)
     */
    public void delete(Long id) {
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con ID: " + id));
        
        horario.setActivo(false);
        horarioRepository.save(horario);
    }
    
    /**
     * Elimina permanentemente un horario
     */
    public void deletePhysically(Long id) {
        if (!horarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Horario no encontrado con ID: " + id);
        }
        horarioRepository.deleteById(id);
    }
    
    /**
     * Verifica si existe solapamiento de horarios
     */
    @Transactional(readOnly = true)
    public boolean existsOverlappingHorario(TipoMarcacion tipo, LocalTime horaInicio, 
                                          LocalTime horaFin, Long excludeId) {
        Long excludeIdValue = excludeId != null ? excludeId : -1L;
        return horarioRepository.existsOverlappingHorario(tipo, horaInicio, horaFin, excludeIdValue);
    }
    
    /**
     * Cuenta horarios activos
     */
    @Transactional(readOnly = true)
    public long countActivos() {
        return horarioRepository.countByActivoTrue();
    }
    
    /**
     * Determina el tipo de marcación basado en la hora actual
     */
    @Transactional(readOnly = true)
    public TipoMarcacion determinarTipoMarcacion(LocalTime horaActual) {
        List<Horario> horariosEnRango = horarioRepository.findByHoraEnRango(horaActual);
        
        if (!horariosEnRango.isEmpty()) {
            // Retorna el primer horario que coincida (debería ser único si no hay solapamientos)
            return horariosEnRango.get(0).getTipo();
        }
        
        // Si no está en ningún rango exacto, buscar el horario más cercano
        Optional<Horario> horarioCercano = encontrarHorarioMasCercano(horaActual);
        if (horarioCercano.isPresent()) {
            return horarioCercano.get().getTipo();
        }
        
        // Si no hay horarios configurados o está muy lejos de cualquier horario
        return TipoMarcacion.FUERA_HORARIO;
    }
    
    /**
     * Calcula el estado de la marcación (puntual, tardanza, fuera de horario)
     */
    @Transactional(readOnly = true)
    public EstadoMarcacion calcularEstadoMarcacion(LocalTime horaActual, TipoMarcacion tipoMarcacion) {
        // Si ya es fuera de horario, retornar directamente
        if (tipoMarcacion == TipoMarcacion.FUERA_HORARIO) {
            return EstadoMarcacion.FUERA_HORARIO;
        }
        
        Optional<Horario> horarioOpt = horarioRepository.findByTipoAndActivoTrue(tipoMarcacion);
        if (horarioOpt.isEmpty()) {
            return EstadoMarcacion.FUERA_HORARIO;
        }
        
        Horario horario = horarioOpt.get();
        
        // Si está dentro del rango configurado, es puntual
        if (horario.estaEnRango(horaActual)) {
            return EstadoMarcacion.PUNTUAL;
        }
        
        // Si está fuera del rango pero es el tipo correcto, es tardanza
        // (esto puede pasar cuando se determina el tipo por proximidad)
        return EstadoMarcacion.TARDANZA;
    }
    
    /**
     * Encuentra el horario más cercano a la hora actual
     */
    @Transactional(readOnly = true)
    public Optional<Horario> encontrarHorarioMasCercano(LocalTime horaActual) {
        List<Horario> horariosActivos = horarioRepository.findByActivoTrueOrderByHoraInicio();
        
        if (horariosActivos.isEmpty()) {
            return Optional.empty();
        }
        
        Horario horarioMasCercano = null;
        long menorDistancia = Long.MAX_VALUE;
        
        for (Horario horario : horariosActivos) {
            // Calcular distancia al centro del rango del horario
            LocalTime centroHorario = calcularCentroHorario(horario.getHoraInicio(), horario.getHoraFin());
            long distancia = Math.abs(horaActual.toSecondOfDay() - centroHorario.toSecondOfDay());
            
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                horarioMasCercano = horario;
            }
        }
        
        // Solo retornar si la distancia es razonable (menos de 2 horas)
        if (menorDistancia <= 7200) { // 2 horas en segundos
            return Optional.of(horarioMasCercano);
        }
        
        return Optional.empty();
    }
    
    /**
     * Calcula el centro de un rango horario
     */
    private LocalTime calcularCentroHorario(LocalTime inicio, LocalTime fin) {
        long inicioSegundos = inicio.toSecondOfDay();
        long finSegundos = fin.toSecondOfDay();
        long centroSegundos = (inicioSegundos + finSegundos) / 2;
        return LocalTime.ofSecondOfDay(centroSegundos);
    }
    
    /**
     * Calcula los minutos de diferencia entre la hora actual y el horario configurado
     */
    @Transactional(readOnly = true)
    public int calcularMinutosDiferencia(LocalTime horaActual, TipoMarcacion tipoMarcacion) {
        Optional<Horario> horarioOpt = horarioRepository.findByTipoAndActivoTrue(tipoMarcacion);
        if (horarioOpt.isEmpty()) {
            return 0;
        }
        
        Horario horario = horarioOpt.get();
        
        // Si está dentro del rango, no hay diferencia
        if (horario.estaEnRango(horaActual)) {
            return 0;
        }
        
        // Calcular diferencia con el punto más cercano del rango
        long horaActualSegundos = horaActual.toSecondOfDay();
        long inicioSegundos = horario.getHoraInicio().toSecondOfDay();
        long finSegundos = horario.getHoraFin().toSecondOfDay();
        
        long diferencia;
        if (horaActualSegundos < inicioSegundos) {
            // Llegó antes del horario
            diferencia = inicioSegundos - horaActualSegundos;
        } else {
            // Llegó después del horario
            diferencia = horaActualSegundos - finSegundos;
        }
        
        return (int) (diferencia / 60); // Convertir a minutos
    }
    
    /**
     * Genera un mensaje contextual para la marcación
     */
    @Transactional(readOnly = true)
    public String generarMensajeMarcacion(LocalTime horaActual, TipoMarcacion tipoMarcacion, EstadoMarcacion estadoMarcacion) {
        String tipoDescripcion = tipoMarcacion.getDescripcion().toLowerCase();
        String horaFormateada = horaActual.toString();
        
        switch (estadoMarcacion) {
            case PUNTUAL:
                return String.format("%s registrada a las %s, puntual 🎉", 
                    capitalize(tipoDescripcion), horaFormateada);
                
            case TARDANZA:
                int minutosTarde = calcularMinutosDiferencia(horaActual, tipoMarcacion);
                if (minutosTarde > 0) {
                    return String.format("%s registrada, llegaste tarde por %d min ⏰", 
                        capitalize(tipoDescripcion), minutosTarde);
                } else {
                    return String.format("%s registrada a las %s", 
                        capitalize(tipoDescripcion), horaFormateada);
                }
                
            case FUERA_HORARIO:
                return String.format("%s registrada fuera de horario a las %s ⚠️", 
                    capitalize(tipoDescripcion), horaFormateada);
                
            default:
                return String.format("%s registrada a las %s", 
                    capitalize(tipoDescripcion), horaFormateada);
        }
    }
    
    /**
     * Capitaliza la primera letra de una cadena
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Verifica si hay horarios configurados para todos los tipos básicos
     */
    @Transactional(readOnly = true)
    public boolean tieneHorariosCompletos() {
        return horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.ENTRADA).isPresent() &&
               horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.SALIDA_ALMUERZO).isPresent() &&
               horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.RETORNO_ALMUERZO).isPresent() &&
               horarioRepository.findByTipoAndActivoTrue(TipoMarcacion.SALIDA).isPresent();
    }
    
    /**
     * Verifica si una hora está dentro de un horario específico
     */
    @Transactional(readOnly = true)
    public boolean estaEnHorario(LocalTime hora, TipoMarcacion tipo) {
        Optional<Horario> horario = horarioRepository.findByTipoAndActivoTrue(tipo);
        return horario.map(h -> h.estaEnRango(hora)).orElse(false);
    }
    
    // Métodos de conversión
    private HorarioDTO convertToDTO(Horario horario) {
        HorarioDTO dto = new HorarioDTO();
        dto.setId(horario.getId());
        dto.setNombre(horario.getNombre());
        dto.setHoraInicio(horario.getHoraInicio());
        dto.setHoraFin(horario.getHoraFin());
        dto.setTipo(horario.getTipo());
        dto.setActivo(horario.getActivo());
        dto.setFechaCreacion(horario.getFechaCreacion());
        return dto;
    }
    
    private Horario convertToEntity(HorarioDTO dto) {
        Horario horario = new Horario();
        horario.setId(dto.getId());
        horario.setNombre(dto.getNombre());
        horario.setHoraInicio(dto.getHoraInicio());
        horario.setHoraFin(dto.getHoraFin());
        horario.setTipo(dto.getTipo());
        horario.setActivo(dto.getActivo());
        return horario;
    }
}